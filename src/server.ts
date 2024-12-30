import bodyParser from "body-parser";
import cors from "cors";
import express from "express";
import carparksRoute from "./routes/v1/carparks";
import vehiclesRoute from "./routes/v1/vehicles";
import recyclingRoute from "./routes/v1/recycling";
import defibrillatorsRoute from "./routes/v1/defibrillators";
import productRecallsRoute from "./routes/v1/product-recalls";
import toiletsRoute from "./routes/v1/toilets";
import eatsafeRoute from "./routes/v1/eatsafe";
import busesRoute from "./routes/v1/bus";
import clsQueuesRoute from "./routes/v1/cls-queues";
import tempChartsRoute from "./routes/v1/charts-temp";
import { ErrorCode } from "./utils/errors/ErrorCode";
import { RouteError } from "./utils/errors/RouteError";
import log from "./utils/log";
import "./utils/puppeteer";
import requestInterceptor from "./utils/request-interceptor";
import { handleError } from "./utils/error-handler";

const app = express();
const port = 8081;

// Disable X-Powered-By header
app.disable("x-powered-by");

// Middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors());

// Used to intercept requests and display incoming requests in console
app.use(requestInterceptor);

// v1 routes
app.use("/v1/carparks", carparksRoute);
app.use("/v1/vehicles", vehiclesRoute);
app.use("/v1/recycling", recyclingRoute);
app.use("/v1/defibrillators", defibrillatorsRoute);
app.use("/v1/product-recalls", productRecallsRoute);
app.use("/v1/toilets", toiletsRoute);
app.use("/v1/bus", busesRoute);
app.use("/v1/eatsafe", eatsafeRoute);
app.use("/v1/cls-queues", clsQueuesRoute);

// Temporary route for hosting chart data that isn't actually
app.use("/v1/charts-temp", tempChartsRoute);

// Handle 404 errors
app.use("*", (req, res) => {
    try {
        throw new RouteError(ErrorCode.NOT_FOUND, 404, "Resource not found");
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

app.listen(port, () => {
    log.info(`Server is running on ${port}`);
});