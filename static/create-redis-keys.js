const redis = require("redis");
const busPassengersJson = require("./bus-passengers-weekly.json");
const defibrillatorsJson = require("./defibrillators.json");
const drivingTestJson = require("./practical-driving-test-results.json");
const recyclingJson = require("./recycling.json");
const roadTrafficJson = require("./road-traffic-weekly.json");
const toiletsJson = require("./toilets.json");
const monthlyRainfallJson = require("./monthly-rainfall.json");

const client = redis.createClient({
    legacyMode: true
});

client.connect();

function addToRedis(redisKey, jsonData) {
    const jsonString = JSON.stringify(jsonData);

    client.set(redisKey, jsonString, (err, reply) => {
        if (err) {
            console.error("Error adding to Redis:", err);
        } else {
            console.log(`Data added: ${redisKey}`);
        }
    });
}

addToRedis("data-toilets:json", toiletsJson);
addToRedis("data-bus-passengers:json", busPassengersJson);
addToRedis("data-defibrillators:json", defibrillatorsJson);
addToRedis("data-driving-test-results:json", drivingTestJson);
addToRedis("data-recycling:json", recyclingJson);
addToRedis("data-road-traffic:json", roadTrafficJson);
addToRedis("data-monthly-rainfall:json", monthlyRainfallJson);