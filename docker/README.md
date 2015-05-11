# Region Coder Docker Config #
To build the image, first copy (don't symlink) the assembly jar to the docker/ subdir as `region-coder-assembly.jar`, then run:
    `docker build -t region-coder .`

Or, if you want to replace old versions:
    `docker build --rm -t region-coder .`

If you are running docker from a VM or boot2docker, it helps to not work in a shared folder (copy the files to a clean local dir first).

## Required Environment Variables ##

* `ZOOKEEPER_ENSEMBLE` - The zookeeper cluster to talk to, in the form of `["10.0.0.1:2181", "10.0.0.2:2818"]`
* `ARK_HOST` - The IP address of the host of the docker container, used for service advertisements.

## Optional Runtime Variables ##
See the [Dockerfile](Dockerfile) for defaults.

* `ENABLE_DEPRESSURIZE` - Should memory depressurization logic be triggered if free memory gets too low
* `MIN_FREE_MEM_PCT` - Free memory threshold that will trigger depressurization (if enabled)
* `TARGET_FREE_MEM_PCT` - Memory depressurization will stop once free memory reaches this threshold
* `ENABLE_GRAPHITE` - Should various metrics information be reported to graphite
* `GRAPHITE_HOST` - The hostname or IP of the graphite server, if enabled
* `GRAPHITE_PORT` - The port number for the graphite server, if enabled
* `LOG_METRICS` - Should various metrics information be logged to the log
