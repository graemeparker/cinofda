#!/bin/bash

cd /usr/local/adfonic
CP=
for i in  /usr/local/adfonic/adfonic-newrelic-plugin/lib/*.jar; do
  CP=$CP:$i
done

APPNAME=adfonic-newrelic-plugin

START_COMMAND="java  -Dnewrelic.platform.config.dir=/usr/local/adfonic/config/newrelic-plugin -cp $CP com.byyd.newrelic.plugin.StartPlugin" 

. /usr/local/adfonic/bin/init-env

export HOSTNAME=`hostname`

startProcess ${APPNAME} ${START_COMMAND}
