import apicache from "apicache";
import { Router } from "express";
import { onlyApiSuccess } from "../../utils/utils";

import busPassengersJson from "../../../static/bus-passengers-weekly.json";
import roadTrafficJson from "../../../static/road-traffic-weekly.json";
import drivingTestJson from "../../../static/practical-driving-test-results.json";

const router = Router();
const cache = apicache.middleware;

router.get("/bus-passengers", cache("1 hour", onlyApiSuccess), async (req, res) => {
    return res.json(busPassengersJson);
});

router.get("/road-traffic", cache("1 hour", onlyApiSuccess), async (req, res) => {
    return res.json(roadTrafficJson);
});

router.get("/driving-test-results", cache("1 hour", onlyApiSuccess), async (req, res) => {
    return res.json(drivingTestJson);
});


export default router;