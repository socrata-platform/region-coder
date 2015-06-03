# region-coder
A service for matching a point or string to the corresponding region in a region dataset.

## Usage
* Build the project: `sbt clean assembly`
* Start the server: `bin/start_region_coder.sh`

## Endpoints

### GET `/version`
Mainly a troubleshooting endpoint that (by virtue of returning a response) tells us that the service is up, as well as providing some useful information about the build that is currently running.

Example request:
```bash
curl -X GET http://localhost:2021/version
```

Example response:
```json
{"name":"region-coder","buildTime":"2015-05-21T17:19:47.860-07:00","scalaVersion":"2.10.5","revision":"v0.1.3-1-g3718e4c5f1","version":"0.1.4-SNAPSHOT","sbtVersion":"0.13.8"}
```

### POST `/regions/<SHAPEFILE_RESOURCE_NAME>/pointcode`
Performs region coding on point input, mapping each point to the ID of the region which encloses it. Output values are returned in the same order as input values. If no enclosing region can be found for a point, its corresponding output value is `null`.

Path parameters:
* `<SHAPEFILE_RESOURCE_NAME>`: NBE resource name of a region dataset. The resource name is the same as the dataset 4x4 with a leading underscore (e.g. `_abcd-1234`).

POST data:
* An array of points, each of which is an array of `[latitude, longitude]`.

Example request:
```bash
# _chic-ago1 is a region dataset of Chicago zip codes
# [-87.632322, 41.883846] and [-87.775757,41.932142] are points in Chicago
# [0,0] is a point in the Atlantic Ocean
curl -X POST -H "Content-Type: application/json" \
  http://localhost:2021/v1/regions/_chic-ago1/pointcode \
  -d '[[-87.632322, 41.883846],[-87.775757,41.932142],[0,0]]'
```
Example response:
```json
[34,52,null]
```

### POST `/regions/<SHAPEFILE_RESOURCE_NAME>/stringcode`
Performs region coding on string input, mapping each string to the ID of the region with a matching value in the given column. Output values are returned in the same order as input values. If no matching region can be found for a point, its corresponding output value is `null`.

Path parameters:
* `<SHAPEFILE_RESOURCE_NAME>`: NBE resource name of a region dataset. The resource name is the same as the dataset 4x4 with a leading underscore (e.g. `_abcd-1234`).

Query parameters:
* `column`: The name of the column in the region dataset to use for input matching.

POST data:
* An array of strings.

Example request:
```bash
# _chic-ago1 is a region dataset of Chicago zip codes
# zipcode is the column of the region dataset which contains zip codes
# "60661" and "60707" are valid Chicago zip codes
# "39211" is a valid Mississippi zip code
# "giraffe" is not a valid zip code
curl -X POST -H "Content-Type: application/json" \
  http://localhost:2021/v1/regions/_chic-ago1/stringcode?column=zipcode \
  -d '["60661","60707","39211","giraffe"]'
```
Example response:
```
[38,51,null,null]
```

## Troubleshooting
* Is your new backend stack running? (soda-fountain, data-coordinator, query-coordinator, secondary-watcher, soql-postgres-adapter)
* Does the resource name you provided in the region coding endpoint exist?
* Are your coordinates formatted correctly? (longitude first, latitude second)
