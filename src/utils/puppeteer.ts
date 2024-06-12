import { Browser, Page } from "puppeteer";
import { Cluster } from "puppeteer-cluster";
import log from "./log";
import { registerVehicleLookupTask } from "./vehicle-lookup-task";

class Puppeteer {
    private browser: Browser;
    private cluster: Cluster;

    constructor() {
        log.info("Launching puppeteer");
        this.load();

        process.stdin.resume();

        process.on("SIGINT", async () => {
            if (this.cluster) {
                log.info("Closing cluster...");
                await this.cluster.close();
            }
            process.exit(0);
        });

    }

    private async load() {
        this.cluster = await Cluster.launch({
            concurrency: Cluster.CONCURRENCY_CONTEXT,
            maxConcurrency: 2,
            timeout: 10 * 1000,
            puppeteerOptions: {
                args: [
                    "--no-sandbox",
                    "--disable-setuid-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-accelerated-2d-canvas",
                    "--disable-gpu"
                ]
            }
        });
        await registerVehicleLookupTask(this.cluster);
    }

    public async newPage(): Promise<Page | null> {
        if (!this.browser) {
            log.error("Tried to open new browser page before initialization");
            return null;
        }
        return this.browser.newPage();
    }

    public async newTask(taskFunction: any) {
        return this.cluster.task(taskFunction);
    }

    public async queue(url: string) {
        return this.cluster.execute(url);
    }
}

export default new Puppeteer();