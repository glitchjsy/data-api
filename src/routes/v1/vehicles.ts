import apicache from "apicache";
import { Request, Response, Router } from "express";
import rateLimit from "express-rate-limit";
import { Vehicle } from "../../models/Vehicle";
import mysql from "../../mysql";
import { handleError } from "../../utils/error-handler";
import puppeteer from "../../utils/puppeteer";
import { onlyApiSuccess, queryDateSql, queryParamNumber, rateLimitHandler, reduceOccurrences } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

const limiter = rateLimit({ windowMs: 2 * 1000, max: 1, handler: rateLimitHandler });
const hourlyLimiter = (limit: number) => rateLimit({ windowMs: 60 * 60 * 1000, max: limit, handler: rateLimitHandler });

router.get("/", [
    /*cache("1 day", onlyApiSuccess),*/
    hourlyLimiter(600)
], async (req: Request, res: Response) => {
    try {
        const dateField = req.query.dateType === "regInJersey" ? "firstRegisteredInJerseyAt" : "firstRegisteredAt";
        const { dateSql, dateParams } = queryDateSql(dateField, req.query.startDate, req.query.endDate);

        const page = queryParamNumber(req.query.page) ?? 1;
        const limit = queryParamNumber(req.query.limit) ?? 30;
        const offset = (page - 1) * limit;

        let baseQuery = `FROM vehicles ${dateSql}`;
        let sqlParams = [...dateParams];

        if (req.query.make) {
            baseQuery += dateSql ? " AND" : " WHERE";
            baseQuery += " make LIKE ?";
            sqlParams.push(`%${req.query.make}%`);
        }
        if (req.query.model) {
            baseQuery += (req.query.make || dateSql) ? " AND" : " WHERE";
            baseQuery += " model LIKE ?";
            sqlParams.push(`%${req.query.model}%`);
        }
        if (req.query.fuelType) {
            baseQuery += (req.query.make || req.query.model || dateSql) ? " AND" : " WHERE";
            baseQuery += " fuelType = ?";
            sqlParams.push(req.query.fuelType);
        }
        if (req.query.color) {
            baseQuery += (req.query.make || req.query.model || req.query.fuelType || dateSql) ? " AND" : " WHERE";
            baseQuery += " color LIKE ?";
            sqlParams.push(`%${req.query.color}%`);
        }

        const countResult = await mysql.execute(`SELECT COUNT(*) AS count ${baseQuery}`, sqlParams);
        const totalItems = countResult[0].count;
        const totalPages = Math.ceil(totalItems / limit);

        const vehicles: Vehicle[] = await mysql.execute(`
            SELECT *
            ${baseQuery} 
            LIMIT ? 
            OFFSET ?`,
            [...sqlParams, limit, offset]
        );

        const mappedVehicles = vehicles.map(v => ({ ...v, id: undefined }));

        res.json({
            pagination: {
                page,
                limit,
                totalPages,
                totalItems
            },
            results: mappedVehicles
        });
    } catch (e: any) {
        handleError(e, req, res);
    }
});

router.get("/stats", [
    hourlyLimiter(500)
], async (req: Request, res: Response) => {
    try {
        const dateField = req.query.dateType === "regInJersey" ? "firstRegisteredInJerseyAt" : "firstRegisteredAt";
        const { dateSql, dateParams } = queryDateSql(dateField, req.query.startDate, req.query.endDate);

        const totalResults = await mysql.execute(`
            SELECT
                COUNT(*) AS totalVehicles,
                COUNT(DISTINCT model) AS distinctModels,
                COUNT(DISTINCT make) AS distinctMakes,
                COUNT(DISTINCT color) AS distictColors
            FROM vehicles
            ${dateSql}
        `, dateParams);
       
        return res.json({
            results: {
                ...totalResults[0]
            }
        });
    } catch (e: any) {
        handleError(e, req, res);
    }
});

router.get("/stats/colors", [
    cache("1 day", onlyApiSuccess),
    hourlyLimiter(500)
], async (req: Request, res: Response) => {
    try {
        const dateField = req.query.dateType === "regInJersey" ? "firstRegisteredInJerseyAt" : "firstRegisteredAt";
        const { dateSql, dateParams } = queryDateSql(dateField, req.query.startDate, req.query.endDate);

        const results = await mysql.execute(`
            SELECT 
                color, COUNT(*) AS occurrences
            FROM vehicles
            ${dateSql}
            GROUP BY color
            ORDER BY occurrences DESC
        `, dateParams);

        const occurrences = reduceOccurrences(results, "color");

        return res.json({
            results: occurrences
        });
    } catch (e: any) {
        handleError(e, req, res);
    }
});

router.get("/stats/makes", [
    cache("1 day", onlyApiSuccess),
    hourlyLimiter(500)
], async (req: Request, res: Response) => {
    try {
        const dateField = req.query.dateType === "regInJersey" ? "firstRegisteredInJerseyAt" : "firstRegisteredAt";
        const { dateSql, dateParams } = queryDateSql(dateField, req.query.startDate, req.query.endDate);

        const results = await mysql.execute(`
            SELECT 
                make, COUNT(*) AS occurrences
            FROM vehicles
            ${dateSql}
            GROUP BY make
            ORDER BY occurrences DESC
        `, dateParams);

        const occurrences = reduceOccurrences(results, "make");

        return res.json({
            results: occurrences
        });
    } catch (e: any) {
        handleError(e, req, res);
    }
});

router.get("/stats/models", [
    cache("1 day", onlyApiSuccess),
    hourlyLimiter(500)
], async (req: Request, res: Response) => {
    try {
        const dateField = req.query.dateType === "regInJersey" ? "firstRegisteredInJerseyAt" : "firstRegisteredAt";
        const { dateSql, dateParams } = queryDateSql(dateField, req.query.startDate, req.query.endDate);

        const page = queryParamNumber(req.query.page) ?? 1;
        const limit = queryParamNumber(req.query.limit) ?? 300;
        const offset = (page - 1) * limit;

        const countResult = await mysql.execute(`
            SELECT 
                COUNT(*) AS count
            FROM (
                SELECT 
                    model, make, COUNT(*) AS occurrences
                FROM vehicles
                ${dateSql}
                GROUP BY model, make
            ) AS subquery    
        `, dateParams);
        const totalItems = countResult[0].count;
        const totalPages = Math.ceil(totalItems / limit);

        const results = await mysql.execute(`
            SELECT
                model, make, COUNT(*) AS occurrences
            FROM vehicles
            ${dateSql}
            GROUP BY model, make
            ORDER BY occurrences DESC
            LIMIT ?
            OFFSET ?
        `, [...dateParams, limit, offset]);

        const occurrences = results.reduce((obj: any, item: any) => {
            const key = item.model === "" ? "Not Specified" : `${item.make} ${item.model}`;
            obj[key] = item.occurrences;
            return obj;
        }, {});

        return res.json({
            pagination: {
                page,
                limit,
                totalPages,
                totalItems
            },
            results: occurrences
        });
    } catch (e: any) {
        handleError(e, req, res);
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
        handleError(e, req, res);
    }
});

export default router;