import apicache from "apicache";
import { Router } from "express";
import mysql from "../../mysql";
import { handleError } from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/", cache("1 hour", onlyApiSuccess), async (req, res) => {
    try {
        const results = await mysql.execute("SELECT * FROM publicAccessDefibrillators");

        const mappedResults = results.map((item: any) => {
            return {
                ...item,
                latitude: item.latitude !== null ? Number(item.latitude) : null,
                longitude: item.longitude !== null ? Number(item.longitude) : null,
            }
        })
        return res.json({ results: mappedResults });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

export default router;