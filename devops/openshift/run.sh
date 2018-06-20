#!/bin/sh
git clone -v $GIT_URL repo
ls -lah repo

# Execute
cd repo && ./gradlew assemble restAssuredTest -DbaseUrl=$TARGET_URL -DbuildVersion=$BUILD_VERSION -DcommonVersion=$COMMON_VERSION -DinfraVersion=$INFRA_VERSION
if [ $? -eq 0 ]; then
  RESULT="SUCCESS"
else 
  RESULT="FAILED"
fi

# Copy test results to persistent volume mount
mkdir -p /mnt/reports/$JOB_NAME/$BUILD_VERSION
cp -R /opt/app-root/repo/web/build/reports/tests/restAssuredTest/* /mnt/reports/$JOB_NAME/$BUILD_VERSION/.

curl -X POST -k -d $RESULT $CALLBACK_URL
