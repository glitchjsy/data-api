import { ErrorCode } from "./ErrorCode";

export class RouteError extends Error {
    public readonly code: ErrorCode;
    public readonly status: number;

    constructor(code: ErrorCode, status: number, message: string) {
        super(message);

        Object.setPrototypeOf(this, new.target.prototype);
        
        this.code = code;
        this.status = status;
    }
}