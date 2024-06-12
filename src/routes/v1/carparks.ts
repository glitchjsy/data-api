import apicache from "apicache";
import { Router } from "express";
import { Carpark } from "../../models/Carpark";
import mysql from "../../mysql";
import redis from "../../redis";
import errorHandler from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/", cache("1 hour", onlyApiSuccess), async (req, res) => {
    const carparks = await mysql.execute(`
        SELECT 
            carparks.*,
            companies.name AS ownerName 
        FROM
            carparks
        LEFT JOIN
            companies ON companies.id = carparks.ownerId
    `) as Carpark[];

    return res.json(carparks.map((c: any) => {
        const carpark = { ...c } as any;

        delete carpark.ownerId;
        delete carpark.ownerName;

        return {
            ...carpark,
            owner: {
                id: c.ownerId,
                name: c.ownerName
            }
        }
    }))
});

router.get("/live-spaces", cache("1 minute", onlyApiSuccess), async (req, res) => {
    const json = await redis.getAsync("data-livespaces:json");

    if (!json) {
        return res.json([]);
    }
    return res.json(JSON.parse(json));
});

router.use(errorHandler);

export default router;