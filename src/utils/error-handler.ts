import { NextFunction, Request, Response } from "express";
import { RouteError } from "./errors/RouteError";
import { ErrorCode } from "./errors/ErrorCode";
import log from "./log";

export function handleError(err: Error, req: Request, res: Response) {
    let routeErr = err instanceof RouteError ? err : new RouteError(
        ErrorCode.SERVER_ERROR,
        req.statusCode ?? 500,
        "An error has occurred"
    );

    log.debug(`Error: (${routeErr.status}) ${err.message}`);

    return res.status(routeErr.status).json({
        error: routeErr.code,
        status: routeErr.status,
        message: routeErr.message
    });
}