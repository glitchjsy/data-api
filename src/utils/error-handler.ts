import { NextFunction, Request, Response } from "express";
import { RouteError } from "./errors/RouteError";
import { ErrorCode } from "./errors/ErrorCode";
import log from "./log";

export default function (err: Error, req: Request, res: Response, next?: NextFunction) {
    let routeErr = err instanceof RouteError ? err : new RouteError(
        ErrorCode.SERVER_ERROR,
        req.statusCode ?? 500,
        err.message
    );

    log.debug(`Error: (${routeErr.status}) ${routeErr.message}`);

    return res.status(routeErr.status).json({
        error: routeErr.code,
        status: routeErr.status,
        message: routeErr.message
    });
}