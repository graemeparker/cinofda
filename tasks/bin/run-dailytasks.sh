#!/bin/sh

. /usr/local/adfonic/bin/init-env

# https://tickets.adfonic.com/browse/AF-558
# This is the dir in which we'll leave timestamp "breadcrumbs" for monitoring.
BREADCRUMB_DIR=/var/log/adfonic/crons
if [ ! -d ${BREADCRUMB_DIR} ]; then
    echo "Creating breadcrumb dir: ${BREADCRUMB_DIR}"
    mkdir ${BREADCRUMB_DIR}
    if [ $? -ne 0 ]; then
        echo "ERROR: failed to create breadcrumb dir: ${BREADCRUMB_DIR}"
        # Proceed...the least we can do is still run stuff
    fi
fi

cd /usr/local/adfonic/tasks

LOGNAME=`getLogName devicelog`
LOGFILE=${ADFONIC_LOG_DIR}/${LOGNAME}
(./bin/download-device-atlas-data && ./bin/distribute-device-atlas-data_to_AWS.sh) >> ${LOGFILE}
if [ $? -eq 0 ]; then
    touch ${BREADCRUMB_DIR}/distribute-device-atlas-data
fi



sh bin/run-ant-task.sh sync-devices
if [ $? -eq 0 ]; then
    touch ${BREADCRUMB_DIR}/sync-devices
fi

# Don't run platform-mapper on rftest01
if [ `hostname` = "lon3proc03" -o `hostname` = "lon3proc03.adfonic.com" -o `hostname` = "lon3proc04" -o `hostname` = "lon3proc04.adfonic.com" ]; then
    sh bin/run-ant-task.sh platform-mapper
    if [ $? -eq 0 ]; then
        touch ${BREADCRUMB_DIR}/platform-mapper
    fi
fi

sh bin/run-ant-task.sh auto-phase-out-mobile-ip-address-range
if [ $? -eq 0 ]; then
    touch ${BREADCRUMB_DIR}/auto-phase-out-mobile-ip-address-range
fi
