import apicache from "apicache";
import { Router } from "express";
import mysql from "../../mysql";
import errorHandler from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/", async (req, res) => {
    // Definitly didnt use chatgpt because i was too lazy to write this
    const results = await mysql.execute(`
       SELECT 
        toilet.*,
        companies.name AS ownerName,
        GROUP_CONCAT(DISTINCT facility.facility ORDER BY facility.id SEPARATOR ", ") AS facilities,
        GROUP_CONCAT(DISTINCT femalePeriodProductsQ.product ORDER BY femalePeriodProductsQ.id SEPARATOR ", ") AS femalePeriodProducts,
        GROUP_CONCAT(DISTINCT malePeriodProductsQ.product ORDER BY malePeriodProductsQ.id SEPARATOR ", ") AS malePeriodProducts
    FROM publicToilets toilet
    LEFT JOIN publicToiletFacilities facility ON toilet.id = facility.publicToiletId
    LEFT JOIN publicToiletPeriodProducts femalePeriodProductsQ ON toilet.id = femalePeriodProductsQ.publicToiletId AND femalePeriodProductsQ.type = "FEMALE"
    LEFT JOIN publicToiletPeriodProducts malePeriodProductsQ ON toilet.id = malePeriodProductsQ.publicToiletId AND malePeriodProductsQ.type = "MALE"
    LEFT JOIN companies ON companies.id = toilet.ownerId
    GROUP BY toilet.id, companies.name;

    `);

    const mappedResults = results.map((item: any) => {
        const facilitiesArray = !item.facilities ? [] : item.facilities.split(',').map((service: any) => service.trim());
        const maleProductsArray = !item.malePeriodProducts ? [] : item.malePeriodProducts.split(',').map((service: any) => service.trim());
        const femaleProductsArray = !item.femalePeriodProducts ? [] : item.femalePeriodProducts.split(',').map((service: any) => service.trim());
        
        const data = {
            id: item.id,
            createdAt: item.createdAt,
            updatedAt: item.updatedAt,
            name: item.name,
            parish: item.parish,
            latitude: item.latitude,
            longitude: item.longitude,
            tenure: item.tenure,
            owner: {
                id: item.ownerId,
                name: item.ownerName
            },
            buildDate: item.buildDate,
            facilities: facilitiesArray
        } as any;

        if (item.female) {
            data.female  = {
                cubicles: item.femaleCubicles,
                handDryers: item.femaleHandDryers,
                sinks: item.femaleSinks,
                periodProducts: femaleProductsArray
            }
        }
        if (item.male) {
            data.male  = {
                cubicles: item.maleCubicles,
                urinals: item.maleUrinals,
                handDryers: item.maleHandDryers,
                sinks: item.maleSinks,
                periodProducts: maleProductsArray
            }
        }

        return data;
    });

    return res.json({ results: mappedResults });
});

router.use(errorHandler);

export default router;