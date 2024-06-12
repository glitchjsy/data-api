import { Request, Response } from "express";
import { ErrorCode } from "./errors/ErrorCode";

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