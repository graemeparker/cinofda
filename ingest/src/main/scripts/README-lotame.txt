*** Instructions for importing Lotame data ***

0. mkdir /var/lib/mysql/incoming
1. Download files from Lotame S3 server. Place in /var/lib/mysql/incoming
% cd /var/lib/mysql/incoming
% s3cmd --access_key=AKIAIKFET3OU56F6WSLA \
--secret_key="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" \
-r get s3://lotame-ldn-mobile-audiences/segments/

2. mkdir /var/lib/mysql/incoming/unzipped
3. mkdir /var/lib/mysql/incoming/unzipped/loaded
4. Place all scripts and compiled Processor.class in unzipped directory
5. Initialize the lotame database using sql/lotame.sql if required, or
just truncate relevant tables to start from scratch:
truncate extract; 
truncate device_country_type;
truncate segment_device;
truncate device;
truncate segment_population;

6. In a screen terminal, run ./bulk.sh. This relies on Processor.class and load.sql.

Data will be created in tables "device" and "segment_device". The .gz
source files will be recompressed and moved to the "loaded"
subdirectory when processing is complete.

7. For Lotame you then need to postprocess to derive country and device type:
source postprocess-lotame.sql

8. You can generate segment counts (not required) by calling:
call proc_calc_all_segments_population();
Output is in the segment_population table.

To generate data for import to MUID, call
do_extract(country_id, lotame_segment_id, muid_segment_id)
for each segment you want. Existing scripts do this for all segments
for specific countries:
 source load-uk.sql
 source load-de.sql
 source load-age.sql
The do_extract function automatically converts raw IDFA to HIFA.

do_extract loads the flattened data to the extract table. You then
need to dump it to TSV:
select * from extract into outfile '/tmp/extract.tsv';

Format of output is

device_id device_type_id	muid_segment_id

You may wish to split the megafile into multiple chunks for more
controlled processing.

To load into MUID.. TODO
