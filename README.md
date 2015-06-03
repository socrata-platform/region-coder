# region-coder
Matches a point or string to the corresponding region in a region dataset

## Usage
Start the server with ```bin/start_region_coder.sh```.

### Endpoints
#### Version endpoint - is the service up?

```
curl -X GET http://localhost:2021/version
```
Example of an expected response:
```
{"name":"region-coder","buildTime":"2015-05-21T17:19:47.860-07:00","scalaVersion":"2.10.5","revision":"v0.1.3-1-g3718e4c5f1","version":"0.1.4-SNAPSHOT","sbtVersion":"0.13.8"}
```
#### Region coding endpoint - E2E test
* Replace {SHAPEFILE_RESOURCE_NAME} below with the NBE resource name of a region dataset on your local machine. For example, if the 4x4 of the region dataset is abcd-1234, the NBE resource name is _abcd-1234.
* Replace the coordinates in the request body with coordinates that correspond to your region dataset. The ones below represent two locations in Chicago.
```
curl -X POST -H "Content-Type: application/json" http://localhost:2021/v1/regions/{SHAPEFILE_RESOURCE_NAME}/pointcode -d '[[-87.632322, 41.883846],[-87.775757,41.932142]]'
```
Example of an expected response:
```
[34,52]
```

## Troubleshooting
* Is your new backend stack running? (soda-fountain, data-coordinator, query-coordinator, secondary-watcher, soql-postgres-adapter)
* Does the resource name you provided in the region coding endpoint exist on your local instance of the new backend?
* Are your coordinates formatted correctly? (longitude first, latitude second)

