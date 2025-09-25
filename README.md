<img src="https://i.imgur.com/LDS7bBP.png">

# [Open Data (API)](https://data.glitch.je)
[![Hits](https://hitcount.dev/p/glitchjsy/data-api.svg)](https://hitcount.dev/p/glitchjsy/data-api)  
The public API for accessing opendata.je.

Recently rewritten in Java!

### Redis keys 
The keys which must be created manually can be done by running the `create-redis-keys.js` file in the `static` directory.

- `data-livespaces:json` - Stores live carpark spaces data, created automatically using `data-fetcher`
- `data-eatsafe:json` - Stores eatsafe information, created automatically using `data-fetcher`
- `data-toilets:json` - Static toilet data, must be created manually
- `data-recycling:json` - Static recycling data, must be created manually
- `data-defibrillators:json` - Static defibrillator locations, must be created manually
- `data-bus-passengers:json` - Static bus passengers data, must be created manually
- `data-road-traffic:json` - Static road traffic data, must be created manually
- `data-driving-test-results:json` - Static practical driving test results, must be created manually
- `data-monthly-rainfall:json` - Static monthly rainfall data, must be created manually