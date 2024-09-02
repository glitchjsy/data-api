import apicache from "apicache";
import { Router } from "express";
import redis from "../../redis";
import { handleError } from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/", cache("1 minute", onlyApiSuccess), async (req, res) => {
    try {
        const json = await redis.getAsync("data-eatsafe:json");

        if (!json) {
            return res.json([]);
        }
        return res.json(JSON.parse(json));
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

export default router;