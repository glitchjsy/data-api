import apicache from "apicache";
import { Router } from "express";
import mysql from "../../mysql";
import { handleError } from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/", cache("1 hour", onlyApiSuccess), async (req, res) => {
    try {
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
                latitude: item.latitude !== null ? Number(item.latitude) : null,
                longitude: item.longitude !== null ? Number(item.longitude) : null,
                services: servicesArray
            }
        });

        return res.json({ results: mappedResults });
    } catch (e: any) {
        return handleError(e, req, res);
    }
});

export default router;