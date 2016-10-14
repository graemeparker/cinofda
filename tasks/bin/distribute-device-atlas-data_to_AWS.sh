#!/bin/bash

FILE=`sed '/^\#/d' /usr/local/adfonic/config/adfonic-tasks.properties | grep 'deviceatlas.s3.upload.local.path=' | tail -n 1 | sed 's/^.*=//;s/^[[:space:]]*//;s/[[:space:]]*$//'`

if [ ! -f ${FILE} ]; then
   echo "ERROR: DeviceAtlas file not found: ${FILE}"
   exit 1
fi


BUCKET_KEY=`sed '/^\#/d' /usr/local/adfonic/config/adfonic-tasks.properties | grep 'deviceatlas.s3.upload.bucket=' | tail -n 1 | sed 's/^.*=//;s/^[[:space:]]*//;s/[[:space:]]*$//'`
OBJECT_KEY=`sed '/^\#/d' /usr/local/adfonic/config/adfonic-tasks.properties | grep 'deviceatlas.s3.upload.key=' | tail -n 1 | sed 's/^.*=//;s/^[[:space:]]*//;s/[[:space:]]*$//'`
AWS_URL=https://$BUCKET_KEY.s3.amazonaws.com/$OBJECT_KEY
S3_ACCESS_KEY=`sed '/^\#/d' /usr/local/adfonic/config/adfonic-tasks.properties | grep 'deviceatlas.s3.upload.accessKey=' | tail -n 1 | sed 's/^.*=//;s/^[[:space:]]*//;s/[[:space:]]*$//'`
S3_SECRET_KEY=`sed '/^\#/d' /usr/local/adfonic/config/adfonic-tasks.properties | grep 'deviceatlas.s3.upload.secretKey=' | tail -n 1 | sed 's/^.*=//;s/^[[:space:]]*//;s/[[:space:]]*$//'`
CONTENT_TYPE="application/gzip"
DATE="$(LC_ALL=C date -u +"%a, %d %b %Y %X %z")"
FILENAME=`basename $FILE`
DATE_TIMESTAMP=`date +%s`
COMPRESSED_FILE=$FILENAME-$DATE_TIMESTAMP.tar.gz

gzip -c $FILE > $COMPRESSED_FILE

if [ ! -f ${COMPRESSED_FILE} ]; then
   echo "ERROR: DeviceAtlas file not found: ${COMPRESSED_FILE}"
   exit 1
fi

echo "File compressed (gzip)"

MD5="$(openssl md5 -binary < "$COMPRESSED_FILE" | base64)"
SIGNATURE="$(printf "PUT\n$MD5\n$CONTENT_TYPE\n$DATE\n/$BUCKET_KEY/$OBJECT_KEY" | openssl sha1 -binary -hmac "$S3_SECRET_KEY" | base64)"
echo "MD5 and Signature generated"

#echo "Configuration used: "
#echo FILE $FILE
#echo BUCKET_KEY $BUCKET_KEY
#echo OBJECT_KEY $OBJECT_KEY
#echo AWS_URL $AWS_URL
#echo S3_ACCESS_KEY $S3_ACCESS_KEY
#echo S3_SECRET_KEY $S3_SECRET_KEY
#echo CONTENT_TYPE $CONTENT_TYPE
#echo DATE $DATE
#echo COMPRESSED_FILE $COMPRESSED_FILE
#echo MD5 $MD5
#echo SIGNATURE $SIGNATURE

echo "Uploading file"
EXIT_CODE=`curl -s -o /dev/null -w "%{http_code}" --insecure -H "Date: $DATE" -H "Authorization: AWS $S3_ACCESS_KEY:$SIGNATURE" -H "Content-Type: $CONTENT_TYPE" -H "Content-MD5: $MD5" -T $COMPRESSED_FILE $AWS_URL`

if [ $EXIT_CODE == "200" ]; then
   echo "File uploaded"
   EXIT_CODE=0 
else
   echo "Error uploading the file (HTTP error code: $EXIT_CODE)"
fi

rm $COMPRESSED_FILE
echo "Compressed file deleted"

exit $EXIT_CODE

