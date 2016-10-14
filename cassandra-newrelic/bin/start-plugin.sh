#!/bin/bash

cd /usr/local/adfonic
for i in  /usr/local/adfonic/adfonic-cassandra-newrelic/lib/*.jar; do
  CP=$CP:$i
done

APPNAME=cassandra-newrelic

START_COMMAND="java  -Dnewrelic.platform.config.dir=/usr/local/adfonic/config/cassandra-newrelic -cp $CP com.byyd.newrelic.cassandra.StartPlugin"

. /usr/local/adfonic/bin/init-env

export HOSTNAME=`hostname`

startProcess ${APPNAME} ${START_COMMAND}
