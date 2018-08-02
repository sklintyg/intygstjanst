#!/bin/bash
# Assign backing service addresses from the outer environment

export DB_USERNAME=${DATABASE_USERNAME:-intyg}
export DB_PASSWORD=${DATABASE_PASSWORD:-intyg}
export DB_NAME=${DATABASE_NAME:-intygstjanst_test}
export DB_SERVER=$MYSQL_SERVICE_HOST
export DB_PORT=$MYSQL_SERVICE_PORT

export ACTIVEMQ_BROKER_USERNAME=${ACTIVEMQ_BROKER_USERNAME:-admin}
export ACTIVEMQ_BROKER_PASSWORD=${ACTIVEMQ_BROKER_PASSWORD:-admin}

export REDIS_PASSWORD=${REDIS_PASSWORD:-redis}
export REDIS_PORT=$REDIS_SERVICE_PORT
export REDIS_HOST=$REDIS_SERVICE_HOST

# dev profile is default for pipeline
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev,testability-api,caching-enabled,wc-hsa-stub}

export CATALINA_OPTS_APPEND="\
-Dconfig.folder=/opt/$APP_NAME/config \
-Dconfig.file=/opt/$APP_NAME/config/intygstjanst.properties \
-Dlogback.file=classpath:logback-ocp.xml \
-Drecipient.config.file=/opt/$APP_NAME/config/intygstjanst-recipients.json \
-Dcertificate.folder=/opt/$APP_NAME/env \
-Dcredentials.file=/opt/$APP_NAME/env/secret-env.properties \
-Dresources.folder=/tmp/resources \
-Dfile.encoding=UTF-8 \
-DbaseUrl=http://${APP_NAME}:8080"
