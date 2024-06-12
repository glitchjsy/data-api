import { Request, Response } from "express";
import log from "./log";

export default function requestInterceptor(req: Request, res: Response, next: Function) {
    res.json = (data: any): any => {
        res.set("Content-Type", "application/json");
        res.send(JSON.stringify(data, null, req.query.pretty === "true" ? 2 : 0));
    }
    log.debug(`Got ${req.method} request - ${req.originalUrl}`);
    next();
};