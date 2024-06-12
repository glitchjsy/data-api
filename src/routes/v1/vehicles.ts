import apicache from "apicache";
import { Request, Response, Router } from "express";
import rateLimit from "express-rate-limit";
import errorHandler from "../../utils/error-handler";
import puppeteer from "../../utils/puppeteer";
import { onlyApiSuccess, rateLimitHandler } from "../../utils/utils";
import mysql from "../../mysql";
import { Vehicle } from "../../models/Vehicle";

const router = Router();
const cache = apicache.middleware;

const limiter = rateLimit({ windowMs: 2 * 1000, max: 1, handler: rateLimitHandler });
const hourlyLimiter = (limit: number) => rateLimit({ windowMs: 60 * 60 * 1000, max: limit, handler: rateLimitHandler });

router.get("/", [
    cache("1 day", onlyApiSuccess),
    hourlyLimiter(600)
], async (req: Request, res: Response) => {
    try {
        const getQueryParam = (param: string | string[] | any | any[] | undefined): number | null => {
            if (typeof param === "string") {
                const parsed = parseInt(param, 10);
                return isNaN(parsed) ? null : parsed;
            }
            return null;
        };

        const page = getQueryParam(req.query.page) ?? 1;
        const limit = getQueryParam(req.query.limit) ?? 30;
        const offset = (page - 1) * limit;

        // Get the total count of vehicles
        const countResult = await mysql.execute("SELECT COUNT(*) AS count FROM vehicles");
        const totalItems = countResult[0].count;
        const totalPages = Math.ceil(totalItems / limit);

        // Get the vehicles for the current page
        const vehicles: Vehicle[] = await mysql.execute("SELECT * FROM vehicles LIMIT ? OFFSET ?", [limit, offset]);

        // Map vehicles and exclude the ID
        const mappedVehicles = vehicles.map(v => ({ ...v, id: undefined }));
        
        res.json({
            page,
            limit,
            totalPages,
            totalItems,
            data: mappedVehicles
        });
    } catch (e: any) {
        errorHandler(e, req, res);
    }
});

router.get("/:plate", [
    cache("1 day", onlyApiSuccess),
    limiter,
    hourlyLimiter(300)
], async (req: Request, res: Response) => {
    try {
        const output = await puppeteer.queue(req.params.plate);
        return res.json(output);
    } catch (e: any) {
        errorHandler(e, req, res);
    }
});

export default router;