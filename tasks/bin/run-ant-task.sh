#!/bin/sh

TASK=$1
START_COMMAND="ant -emacs -f `dirname $0`/../build.xml ${TASK}"

. /usr/local/adfonic/bin/init-env

startProcessInForeground ${TASK} ${START_COMMAND}
exit $?