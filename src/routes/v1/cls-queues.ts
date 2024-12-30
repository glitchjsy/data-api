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

router.get("/", cache("1 minute", onlyApiSuccess), async (req, res) => {
    try {
        const json = await redis.getAsync("data-clsqueues:json");

        if (!json) {
            return res.json([]);
        }

        return res.json(JSON.parse(json));
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

router.get("/dates", async (req, res) => {
    try {
        const results = await mysql.execute("SELECT DISTINCT DATE_FORMAT(createdAt, '%Y-%m-%d') AS date FROM liveClsQueuesData ORDER BY date DESC");
        const dates = results.map((row: any) => row.date);

        return res.json({ results: dates });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

router.get("/graph", async (req, res) => {
    const results = await mysql.execute(`
        SELECT * FROM liveClsQueuesData
        WHERE DATE(createdAt) = ?
        ORDER BY createdAt DESC
    `, [req.query.date]);
    return res.json(results);
});

export default router;