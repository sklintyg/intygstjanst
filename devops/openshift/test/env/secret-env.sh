#!/bin/bash
# Assign backing service addresses from the outer environment

export DB_USERNAME=${DATABASE_USERNAME-intyg}
export DB_PASSWORD=${DATABASE_PASSWORD-intyg}
export DB_NAME=${DATABASE_NAME-intygstjanst_test}
export DB_SERVER=$MYSQL_SERVICE_HOST
export DB_PORT=$MYSQL_SERVICE_PORT

export JMS_DESTINATION_QUEUE_NAME=test.statistik.utlatande.queue
export JMS_BROKER_USERNAME=${ACTIVEMQ_BROKER_USERNAME-admin}
export JMS_BROKER_PASSWORD=${ACTIVEMQ_BROKER_PASSWORD-admin}

export REDIS_PASSWORD=${REDIS_PASSWORD-redis}
export REDIS_PORT=$REDIS_SERVICE_PORT
export REDIS_HOST=$REDIS_SERVICE_HOST

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE-test,caching-enabled,it-fk-stub,hsa-stub}"

export CATALINA_OPTS_APPEND="\
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
-Dconfig.folder=/opt/$APP_NAME/config \
-Dconfig.file=/opt/$APP_NAME/config/intygstjanst.properties \
-Dlogback.file=/opt/$APP_NAME/config/intygstjanst-logback.xml \
-Drecipients.file=/opt/$APP_NAME/config/intygstjanst-recipients.json \
-Dcertificate.folder=/opt/$APP_NAME/env \
-Dcredentials.file=/opt/$APP_NAME/env/secret-env.properties \
-Dresources.folder=/tmp/resources \
-Dfile.encoding=UTF-8 \
-DbaseUrl=http://${APP_NAME}:8080"
