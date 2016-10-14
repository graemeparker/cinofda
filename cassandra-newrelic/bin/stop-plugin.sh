#!/bin/bash

APPNAME=cassandra-newrelic
STOP_COMMAND=

. /usr/local/adfonic/bin/init-env

stopProcess ${APPNAME} ${STOP_COMMAND}
