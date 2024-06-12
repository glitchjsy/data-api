import { Page } from "puppeteer";
import { Cluster } from "puppeteer-cluster";
import crypto from "crypto";
import { RouteError } from "./errors/RouteError";
import { ErrorCode } from "./errors/ErrorCode";
import log from "./log";
import { VehicleFuelType, VehicleType, VehicleWithLookupData } from "../models/Vehicle";
import mysql from "../mysql";

export async function registerVehicleLookupTask(cluster: Cluster) {
    await cluster.task(async ({ page, data: url }: { page: Page, data: any }) => {
        // Relay browser console messages to terminal
        page.on("console", c => {
            if (c.text().includes("net::ERR_FAILED")) {
                return;
            }
            log.debug(`[puppeteer] ${c.text()}`);
        });

        
        // Don't load any resources
        await page.setRequestInterception(true);

        page.on("request", (req) => {
            if (req.resourceType() !== "document") {
                return req.abort();
            }
            return req.continue();
        });

        // Go to search page
        await page.goto("https://vehicle-search.gov.je");

        // Input the number plate
        await page.focus("input[name=plate]");
        await page.keyboard.type(url);

        // Submit
        await Promise.all([
            await page.click("input[type=submit]", { delay: 0 }),
            await page.waitForNetworkIdle()
        ]);

        const titleElement = await page.$(".title");
        const title = await titleElement?.evaluate(el => el.textContent);

        if (title === "No Vehicle Found") {
            throw new RouteError(ErrorCode.NOT_FOUND, 404, "No vehicle found");
        }

        const vehicleData = await page.evaluate(() => {
            const rows = document.getElementsByClassName("detail-row");

            if (rows.length === 0) {
                return null;
            }

            return Array.from(rows).map(row => {
                const cells = row.getElementsByTagName("td");

                return {
                    key: cells[0].textContent,
                    value: cells[1].textContent
                }
            })
        });

        if (vehicleData === null) {
            return null;
        }

        const parseDate = (dateString: string) => {
            const formattedDate = dateString.replace(/(\d+) (\w+) (\d+)/, (match, day, month, year) => {
                var months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
                var monthNumber = ('0' + (months.indexOf(month) + 1)).slice(-2);
                return day + '/' + monthNumber + '/' + year;
            });
            return formattedDate;
        }

        const parseOwnersAndTraders = (input: string) => {
            const regex = /^(\d+)\s*Owner(?:s)?(?:\s*\(incl\.\s*(\d+)\s*Trader(?:s)?\))?$/;
            const match = input.match(regex);

            let result = { owners: 0, traders: 0 };

            if (match) {
                result.owners = parseInt(match[1], 10);

                if (match[2]) {
                    result.traders = parseInt(match[2], 10);
                }
            }
            return result;
        }

        const mapFuelType = (type: string) => {
            switch (type) {
                case "Heavy Oil": return VehicleFuelType.HEAVY_OIL;
                case "Petrol": return VehicleFuelType.PETROL;
                case "Hybrid Electric": return VehicleFuelType.HYBRID_ELECTRIC;
                case "Electric": return VehicleFuelType.ELECTRIC;
                case "Gas": return VehicleFuelType.GAS;
                case "Electric Diesel": return VehicleFuelType.DIESEL_ELECTRIC;
                case "Ga Bi Fuel": return VehicleFuelType.GAS_BI_FUEL;
                case "Steam": return VehicleFuelType.STEAM;
            }
            return VehicleFuelType.UNKNOWN;
        }

        /*
        5-Door Hatchback
        3-Door Hatchback
        Moped
        Motorcycle
        Estate
        (GOODS UNDER 1525KG)
        Saloon
        Car-Derived Van
        Convertible
        Tipper
        Coupe
        Agric. Tractor
        4-Door Saloon
        Motor Home/ Caravan
        Scooter
        Light 4x4 Utility
        */

        const mapVehicleType = (type: string) => {
            switch (type) {
                case "Light Commercial Vehicles < 3,500 kgs": return VehicleType.LIGHT_COMMERCIAL;
                case "Agricultural": return VehicleType.AGRICULTURAL;
                case "Cars": return VehicleType.CAR;
                case "Vehicles > 3,500 kgs": return VehicleType.MEDIUM_WEIGHT;
                case "Motorcycles, Mopeds, Scooters and Tricycles": return VehicleType.MOTORCYCLE;
                case "Light 4x4 Utilities": return VehicleType.LIGHT_4X4_UTILITY;
                case "Vehicles > 7,500 kgs": return VehicleType.HEAVY_WEIGHT;
                case "Works Truck": return VehicleType.WORKS_TRUCK;
                case "Minibuses, Buses and Coaches": return VehicleType.BUS;
                case "Road Maintenance and Construction": return VehicleType.CONSTRUCTION;
                case "Motor Caravan": return VehicleType.MOTOR_CARAVAN;
                case "Motor-Drawn Trailer": return VehicleType.MOTOR_DRAWN_TRAILER;
            }
            return VehicleType.UNKNOWN;
        }

        const findModelFromHash = async (hash: string) => {
            const result = await mysql.execute("SELECT model FROM vehicles WHERE hash = ?", [hash]) as any;

            if (!result || result.length === 0) {
                return null;
            }
            return result[0]?.model ?? null;
        }

        const convertData = async (input: any[]) => {
            const output = {} as VehicleWithLookupData;

            input.forEach(item => {
                switch (item.key) {
                    case "Make":
                        output.make = item.value;
                        break;
                    case "Type":
                        output.type = item.value;
                        break;
                    case "Colour":
                        output.color = item.value;
                        break;
                    case "Cylinder capacity":
                        output.cylinderCapacity = item.value === "" ? null : parseInt(item.value.split(" ")[0]);
                        break;
                    case "Weight":
                        output.weight = item.value;
                        break;
                    case "CO₂ emissions":
                        output.co2Emissions = item.value === "" ? null : parseInt(item.value.split(" ")[0]);
                        break;
                    case "Fuel type":
                        output.fuelType = mapFuelType(item.value);
                        break;
                    case "Date of first registration":
                        output.firstRegisteredAt = parseDate(item.value);
                        break;
                    case "Date registered in Jersey":
                        output.firstRegisteredInJerseyAt = parseDate(item.value);
                        break;
                    case "Number of previous owners":
                        const { owners, traders } = parseOwnersAndTraders(item.value);
                        output.previousOwners = owners;
                        output.previousTraders = traders;
                        break;
                    default:
                        break;
                }
            });

            const hash = `${output.make}-${output.fuelType}-${output.firstRegisteredAt}-${output.firstRegisteredInJerseyAt}-${output.cylinderCapacity}-${output.co2Emissions}`;
            const hex = crypto.createHash("sha256").update(hash).digest("hex");
            const model = await findModelFromHash(hex);

            output.hash = hex;
            output.model = model;
            return output;
        }

        return await convertData(vehicleData);
    });
}