#!/bin/bash
for file in ../*.gz 
do
date
echo Unzipping $file
gzip -dc $file > ${file/..\//}.tsv
lastId=$(cat last_id)
date
echo Processing with last_id $lastId...
java Processor ${file/..\//}.tsv $lastId > last_id
mv $file loaded/
rm -f ${file/..\//}.tsv
mysql lotame < load.sql
done
