FROM socrata/java8-focal

RUN apt-get -y update && apt-get -y install jq

# Service specific settings.
ENV SERVER_NAME region-coder
ENV SERVICE_PORT 2021
EXPOSE 2021
ENV JMX_PORT 2099
EXPOSE 2099

# Some paths/locations we care about
ENV SERVER_ROOT      /srv/${SERVER_NAME}/
ENV SERVER_CONFIG    ${SERVER_NAME}.conf
ENV LOGBACK_CONFIG   ${SERVER_NAME}-logback.xml
ENV SERVER_ARTIFACT  ${SERVER_NAME}-assembly.jar

# Environment Settings.
ENV JAVA_XMX 8192m
ENV MIN_FREE_MEM_PCT 15
ENV TARGET_FREE_MEM_PCT 30
ENV LOG_METRICS false
ENV ENABLE_GRAPHITE false
ENV GRAPHITE_HOST 0.0.0.0
ENV GRAPHITE_PORT 0
ENV ENABLE_DEPRESSURIZE false
ENV THREAD_POOL_LIMIT 10

# Log Settings.
ENV LOG_LEVEL info

# Add other required resources to the container
ADD ship.d /etc/ship.d
ADD ${SERVER_CONFIG}.j2 /etc/
ADD ${LOGBACK_CONFIG}.j2 /etc/
ADD ${SERVER_ARTIFACT} ${SERVER_ROOT}
