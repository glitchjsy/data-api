import apicache from "apicache";
import { Router } from "express";
import { onlyApiSuccess } from "../../utils/utils";

import busPassengersJson from "../../../static/bus-passengers-weekly.json";
import roadTrafficJson from "../../../static/road-traffic-weekly.json";

const router = Router();
const cache = apicache.middleware;

router.get("/bus-passengers", cache("1 hour", onlyApiSuccess), async (req, res) => {
    return res.json(busPassengersJson);
});

router.get("/road-traffic", cache("1 hour", onlyApiSuccess), async (req, res) => {
    return res.json(roadTrafficJson);
});

export default router;