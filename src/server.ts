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
import errorHandler from "./utils/error-handler";
import { ErrorCode } from "./utils/errors/ErrorCode";
import { RouteError } from "./utils/errors/RouteError";
import log from "./utils/log";
import "./utils/puppeteer";
import requestInterceptor from "./utils/request-interceptor";

const app = express();
const port = 8080;

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

// Handle 404 errors
app.use("*", (req, res) => {
    throw new RouteError(ErrorCode.NOT_FOUND, 404, "Resource not found");
});

app.use(errorHandler);

app.listen(port, () => {
    log.info(`Server is running on ${port}`);
});