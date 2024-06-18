import { Request, Response } from "express";
import log from "./log";
import { ErrorCode } from "./errors/ErrorCode";

export default function requestInterceptor(req: Request, res: Response, next: Function) {
    if (!req.headers["User-Agent"]) {
        return res.status(400).json({
            error: ErrorCode.MISSING_USER_AGENT,
            status: 400,
            message: "Missing user agent"
        });
    }
    res.json = (data: any): any => {
        res.set("Content-Type", "application/json");
        res.send(JSON.stringify(data, null, req.query.pretty === "true" ? 2 : 0));
    }
    log.debug(`Got ${req.method} request - ${req.originalUrl}`);
    next();
};