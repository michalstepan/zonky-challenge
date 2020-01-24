# Zonky Challenge

Author: Michal Stepan

This application regularly checks Zonky marketplace for new loans and prints 
them to the console output.

### Application properties
`app.zonky.fetchInterval` - marketplace fetch interval in ms

`app.zonky.fetchSize` - number of loans fetched in single request - can be optimized network
throughput, default value is 20.

Other properties describes the REST endpoint and required setup.

### Limitations
Zonky API offers serves 1 request per second at maximum.
Thus when fetching more frequently, error will be received. 
To prevent this from occur, Spring Retry is used and set
up to standard backoff of 1 second.

### Build
- Build via included maven wrapper with `maven clean package`
- Run via `java -jar target/zonky-0.0.1-SNAPSHOT.jar`