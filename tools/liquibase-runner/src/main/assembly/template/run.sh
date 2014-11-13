#!/bin/sh
java -jar lib/${project.build.finalName}-jar-with-dependencies.jar --changeLogFile="changelog/changelog.xml" --contexts=none update
