import apicache from "apicache";
import { Router } from "express";
import mysql from "../../mysql";
import errorHandler from "../../utils/error-handler";
import { onlyApiSuccess, queryParamNumber } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/",  async (req, res) => {
    const page = queryParamNumber(req.query.page) ?? 1;
    const limit = queryParamNumber(req.query.limit) ?? 50;
    const offset = (page - 1) * limit;

    const countResult = await mysql.execute("SELECT COUNT(*) AS count FROM productRecalls");
    const totalItems = countResult[0].count;
    const totalPages = Math.ceil(totalItems / limit);
    
    const results = await mysql.execute(`
        SELECT * FROM productRecalls
        LIMIT ?
        OFFSET ?
    `, [limit, offset]);
    return res.json({
        pagination: {
            page,
            limit,
            totalPages,
            totalItems
        },
        results
    });
});

router.use(errorHandler);

export default router;