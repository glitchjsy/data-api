import { Request, Response } from "express";
import { ErrorCode } from "./errors/ErrorCode";
import { RouteError } from "./errors/RouteError";

const DATE_FORMAT = /^\d{4}-\d{2}-\d{2}$/;

export const VEHICLE_DATA_LAST_UPDATED = "2024-04-08T23:00:00.000Z";

export function rateLimitHandler(req: Request, res: Response) {
    return res.status(429).json({
        error: ErrorCode.RATE_LIMITED,
        status: 429,
        message: "Too many requests. Please try again later."
    });
}

export function onlyApiSuccess(req: Request, res: Response) {
    return res.statusCode === 200 || res.statusCode === 201;
}

export function queryDateSql(dateField: string, startDate?: any, endDate?: any) {
    const conditions = [];
    const params = [];

    if (startDate) {
        if (!DATE_FORMAT.test(startDate)) {
            throw new RouteError(ErrorCode.INVALID_REQUEST, 400, "Invalid start date. Please use the format YYYY/MM/DD.");
        }
        conditions.push(`${dateField} >= ?`);
        params.push(startDate);
    }
    if (endDate) {
        if (!DATE_FORMAT.test(endDate)) {
            throw new RouteError(ErrorCode.INVALID_REQUEST, 400, "Invalid end date. Please use the format YYYY/MM/DD.");
        }
        conditions.push(`${dateField} <= ?`);
        params.push(endDate);
    }

    return {
        dateSql: conditions.length ? `WHERE ${conditions.join(" AND ")}` : "",
        dateParams: params
    }
}

export function queryParamNumber(param: string | string[] | any | any[] | undefined): number | null {
    if (typeof param === "string") {
        const parsed = parseInt(param, 10);
        return isNaN(parsed) ? null : parsed;
    }
    return null;
}

export function reduceOccurrences(results: any[], field: string) {
    return results.reduce((obj: any, item: any) => {
        const key = item[field] === "" ? "Not Specified" : item[field];
        obj[key] = item.occurrences;
        return obj;
    }, {});
}