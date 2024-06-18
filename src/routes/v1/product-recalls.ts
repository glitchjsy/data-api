import apicache from "apicache";
import { Router } from "express";
import mysql from "../../mysql";
import errorHandler from "../../utils/error-handler";
import { onlyApiSuccess } from "../../utils/utils";

const router = Router();
const cache = apicache.middleware;

router.get("/",  async (req, res) => {
    const results = await mysql.execute(`
        SELECT * FROM productRecalls
    `);
    return res.json({ results });
});

router.use(errorHandler);

export default router;