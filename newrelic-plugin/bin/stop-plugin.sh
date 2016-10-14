#!/bin/bash

APPNAME=adfonic-newrelic-plugin
STOP_COMMAND=

. /usr/local/adfonic/bin/init-env

stopProcess ${APPNAME} ${STOP_COMMAND}
