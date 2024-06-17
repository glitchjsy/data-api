import apicache from "apicache";
import { Router } from "express";
import { Carpark } from "../../models/Carpark";
import mysql from "../../mysql";
import redis from "../../redis";
import errorHandler from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/", cache("1 hour", onlyApiSuccess), async (req, res) => {
    const results = await mysql.execute(`
        SELECT 
            rc.*,
            GROUP_CONCAT(rs.service ORDER BY rs.id SEPARATOR ", ") AS services
        FROM recyclingCentres rc
        LEFT JOIN recyclingCentreServices rs ON rc.id = rs.recyclingCentreId
        GROUP BY rc.id
    `);

    const mappedResults = results.map((item: any) => {
        const servicesArray = item.services.split(',').map((service: any) => service.trim());
        
        return {
            ...item,
            services: servicesArray
        }
    });

    return res.json({ results: mappedResults });
});

router.use(errorHandler);

export default router;