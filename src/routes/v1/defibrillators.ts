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
    const results = await mysql.execute("SELECT * FROM publicAccessDefibrillators");

    const mappedResults = results.map((item: any) => {
        return {
            ...item,
            latitude: item.latitude !== null ? Number(item.latitude) : null,
            longitude: item.longitude !== null ? Number(item.longitude) : null,
        }
    })
    return res.json({ results: mappedResults });
});

router.use(errorHandler);

export default router;