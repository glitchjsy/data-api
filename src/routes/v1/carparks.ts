import apicache from "apicache";
import { Router } from "express";
import { Carpark } from "../../models/Carpark";
import mysql from "../../mysql";
import redis from "../../redis";
import { handleError } from "../../utils/error-handler";
import { ErrorCode } from "../../utils/errors/ErrorCode";
import { RouteError } from "../../utils/errors/RouteError";
import { onlyApiSuccess } from "../../utils/utils";
import log from "../../utils/log";

const router = Router();
const cache = apicache.middleware;

const uuidRegex = /^[0-9A-F]{8}-[0-9A-F]{4}-[4][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i;

router.get("/", cache("1 hour", onlyApiSuccess), async (req, res) => {
    try {
        const carparks = await mysql.execute(`
            SELECT 
                carparks.*,
                companies.name AS ownerName 
            FROM
                carparks
            LEFT JOIN
                companies ON companies.id = carparks.ownerId
        `) as Carpark[];

        const mappedCarparks = carparks.map((c: any) => {
            const carpark = { ...c } as any;

            delete carpark.ownerId;
            delete carpark.ownerName;

            return {
                ...carpark,
                multiStorey: Boolean(c.multiStorey),
                latitude: carpark.latitude !== null ? Number(carpark.latitude) : null,
                longitude: carpark.longitude !== null ? Number(carpark.longitude) : null,
                owner: {
                    id: c.ownerId,
                    name: c.ownerName
                }
            }
        });
        return res.json({ results: mappedCarparks });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

router.get("/live-spaces", cache("1 minute", onlyApiSuccess), async (req, res) => {
    try {
        const json = await redis.getAsync("data-livespaces:json");

        if (!json) {
            return res.json([]);
        }

        const parsed = JSON.parse(json);
        let output = [];

        if (req.query.includeCarparkInfo === "true") {
            for (const spaceData of parsed.results) {
                try {
                    const result = await mysql.execute(`
                        SELECT 
                            carparks.*,
                            companies.name AS ownerName 
                        FROM
                            carparks
                        LEFT JOIN
                            companies ON companies.id = carparks.ownerId
                        WHERE
                           carparks.liveTrackingCode = ?
                    `, [spaceData.code]);

                    if (result.length === 0) {
                        log.warn(`Carpark with live tracking code ${spaceData.code} not found`);
                        output.push({ ...spaceData, carparkInfo: null });
                        continue;
                    }

                    const carpark = result[0];

                    delete carpark.ownerId;
                    delete carpark.ownerName;

                    output.push({
                        ...spaceData,
                        carparkInfo: carpark
                    });
                } catch (e: any) {
                    log.debug(`Error: ${e.message}`);
                    output.push({ ...spaceData, carparkInfo: null });
                    continue;
                }
            }
        } else {
            output = parsed.results;
        }

        return res.json({
            results: output
        });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

router.get("/live-spaces/dates", async (req, res) => {
    try {
        const results = await mysql.execute("SELECT DISTINCT DATE_FORMAT(createdAt, '%Y-%m-%d') AS date FROM liveParkingSpaces ORDER BY date DESC");
        const dates = results.map((row: any) => row.date);

        return res.json({ results: dates });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

router.get("/test-spaces", async (req, res) => {
    const results = await mysql.execute(`
        SELECT * FROM liveParkingSpaces
        WHERE DATE(createdAt) = ?
        ORDER BY createdAt DESC
    `, [req.query.date]);
    return res.json(results);
});

router.get("/:idOrCode", cache("1 hour", onlyApiSuccess), async (req, res) => {
    try {
        const idOrCode = req.params.idOrCode;

        if (!idOrCode) {
            throw new RouteError(ErrorCode.INVALID_REQUEST, 400, "Missing Carpark ID or Live Tracking Code");
        }

        const isUuid = uuidRegex.test(idOrCode);

        const carparkResult = await mysql.execute(`
            SELECT 
                carparks.*,
                companies.name AS ownerName 
            FROM
                carparks
            LEFT JOIN
                companies ON companies.id = carparks.ownerId
            WHERE
                ${isUuid ? "carparks.id" : "carparks.liveTrackingCode"} = ?
        `, [idOrCode]);

        if (carparkResult.length === 0) {
            throw new RouteError(ErrorCode.NOT_FOUND, 404, "Carpark not found");
        }

        const carpark = carparkResult[0];

        delete carpark.ownerId;
        delete carpark.ownerName;

        return res.json({
            ...carpark,
            multiStorey: Boolean(carpark.multiStorey),
            latitude: carpark.latitude !== null ? Number(carpark.latitude) : null,
            longitude: carpark.longitude !== null ? Number(carpark.longitude) : null,
            owner: {
                id: carpark.ownerId,
                name: carpark.ownerName
            }
        });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

export default router;