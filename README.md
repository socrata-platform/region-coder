# region-coder
Matches a point or string to the corresponding region in a region dataset

## Usage
Start the server with ```bin/start_region_coder.sh```.

### Endpoints
#### Version endpoint
Mainly a troubleshooting endpoint that (by virtue of returning a response) tells us that the service is up, as well as providing some useful information aabout the build that is currently running.
```
curl -X GET http://localhost:2021/version
```
Example of an expected response:
```
{"name":"region-coder","buildTime":"2015-05-21T17:19:47.860-07:00","scalaVersion":"2.10.5","revision":"v0.1.3-1-g3718e4c5f1","version":"0.1.4-SNAPSHOT","sbtVersion":"0.13.8"}
```
#### Region code on point endpoint
Maps each point in an array of points to a region ID in the specified region dataset, based on which polygon in the dataset the point falls into
* Replace {SHAPEFILE_RESOURCE_NAME} below with the NBE resource name of a region dataset on your local machine. For example, if the 4x4 of the region dataset is abcd-1234, the NBE resource name is _abcd-1234.
* Replace the coordinates in the request body with coordinates that correspond to your region dataset. The first two below represent two locations in Chicago, while the last one is a location in the Atlantic Ocean.
```
curl -X POST -H "Content-Type: application/json" http://localhost:2021/v1/regions/{SHAPEFILE_RESOURCE_NAME}/pointcode -d '[[-87.632322, 41.883846],[-87.775757,41.932142],[0,0]]'
```
Example of an expected response:
```
[34,52,null]
```
Notes:
* `34` and `52` are the IDs of the matching regions.
* `null` indicates that the point did not match any region in the specified dataset.
* The IDs are returned in the same order as the points were provided in the request.
#### Region code on string endpoint
Maps each string in an array of strings to a region ID in the specified region dataset, matching the provided string on the specified column in the region dataset.
* Replace {SHAPEFILE_RESOURCE_NAME} below with the NBE resource name of a region dataset on your local machine. For example, if the 4x4 of the region dataset is abcd-1234, the NBE resource name is _abcd-1234.
* Replace the strings in the request body with coordinates that correspond to your region dataset. The first two below are zip codes in Chicago, while the third is an invalid zip code.
```
curl -X POST -H "Content-Type: application/json" http://localhost:2021/v1/regions/{SHAPEFILE_RESOURCE_NAME}/stringcode?column={REGION_STRING_COLUMN} -d '["60661","60707","giraffe"]'
```
Example of an expected response:
```
[38,51,null]
```
Notes:
* `38` and `51` are the IDs of the matching regions.
* `null` indicates that the string did not match any value in the specified dataset column.
* The IDs are returned in the same order as the points were provided in the request.
## Troubleshooting
* Is your new backend stack running? (soda-fountain, data-coordinator, query-coordinator, secondary-watcher, soql-postgres-adapter)
* Does the resource name you provided in the region coding endpoint exist on your local instance of the new backend?
* Are your coordinates formatted correctly? (longitude first, latitude second)

