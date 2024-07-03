import { Router } from "express";
import apicache from "apicache";
import { onlyApiSuccess } from "../../utils/utils";
import redis from "../../redis";

const router = Router();
const cache = apicache.middleware;

router.get("/", cache("1 minute", onlyApiSuccess), async (req, res) => {
    const json = await redis.getAsync("data-eatsafe:json");

    if (!json) {
        return res.json([]);
    }
    return res.json(JSON.parse(json));
});

export default router;