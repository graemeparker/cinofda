cd src/main/java
java StatiqProcessor /path/to/PREDEFINED/segments.csv

Generates segments.out, device.out
Move these to /tmp

Save the old data:
select * from extract into outfile '/tmp/statiq-old-all.tsv';

Then start from scratch:
drop database statiq;
create database statiq;
use statiq
source statiq.sql

Load the data:
load data infile '/tmp/device.out' into table device;
load data infile '/tmp/segments.out' ignore into table segment_device;

Run the extraction:
source extract-statiq.sql

---
If there are new segments:

For muid.segment:
awk -F',' '{ print "('"'"'" $2 "'"'"', " $1", 16387, 1, now())," }' < segments_taxonomy.csv

insert into segment (segment_reference, remote_segment_id, segment_source_id, segment_type_id, created_at) values
('Ambitious Professionals', 1, 16387, 1, now()), 
('CineManiacs', 2, 16387, 1, now()), 
('Health and Fitness', 3, 16387, 1, now()), 
('Music Lovers', 4, 16387, 1, now()), 
('Regular Vacationers', 5, 16387, 1, now()), 
('Tech Savvy', 6, 16387, 1, now());

insert into segment (segment_reference, remote_segment_id, segment_source_id, segment_type_id, created_at) values
('Young Families', 7, 16387, 1, now()),
('Outdoor', 8, 16387, 1, now()),
('Shopping Savvy', 9, 16387, 1, now()),
 ('Sport Followers', 10, 16387, 1, now()),
('Extravagant Lifestyle', 11, 16387, 1, now()),
('Tradesmen', 12, 16387, 1, now()),
('Financial Elite', 13, 16387, 1, now()),
('Fashion Shoppers', 14, 16387, 1, now()),
('Foodies', 15, 16387, 1, now()),
('Golfers', 16, 16387, 1, now()),
('High Property Sales', 17, 16387, 1, now()),
('Luxury Cars Owners', 18, 16387, 1, now()),
('Students', 19, 16387, 1, now()),
('Supermarket Shoppers', 20, 16387, 1, now());

insert into segment (segment_reference, remote_segment_id,
segment_source_id, segment_type_id, created_at) values
('Business Commuters', 21, 16387, 1, now()),
('Business Travellers', 22, 16387, 1, now()),
('Night Life', 23, 16387, 1, now()),
('Bread Line Families', 24, 16387, 1, now()),
('Wealthy Retirees', 25, 16387, 1, now()),
('State Dependent', 26, 16387, 1, now()),
('Young Cosmopolitans', 27, 16387, 1, now()),
('Content Retirees', 28, 16387, 1, now()),
('Low Income Households', 29, 16387, 1, now());


For DMP_SELECTOR:
awk -F',' '{ print "(16387, 32780, '"'"'" $2 "'"'"', 716886, 0, 163876, " $1", 1.06, 0.96)," }' < segments_taxonomy.csv

insert into DMP_SELECTOR (DMP_VENDOR_ID, DMP_ATTRIBUTE_ID, NAME, MUID_SEGMENT_ID,
HIDDEN, EXTERNAL_ID, DISPLAY_ORDER, DATA_RETAIL, DATA_WHOLESALE)
values
(16387, 32780, 'Young Families', 716887, 0, 163877, 7, 1.06, 0.96),
(16387, 32780, 'Outdoor', 716888, 0, 163878, 8, 1.06, 0.96),
(16387, 32780, 'Shopping Savvy', 716889, 0, 163879, 9, 1.06, 0.96),
(16387, 32780, 'Sport Followers', 716890, 0, 163880, 10, 1.06, 0.96),
(16387, 32780, 'Extravagant Lifestyle', 716891, 0, 163881, 11, 1.06, 0.96),
(16387, 32780, 'Tradesmen', 716892, 0, 163882, 12, 1.06, 0.96),
(16387, 32780, 'Financial Elite', 716893, 0, 163883, 13, 1.06, 0.96),
(16387, 32780, 'Fashion Shoppers', 716894, 0, 163884, 14, 1.06, 0.96),
(16387, 32780, 'Foodie', 716895, 0, 163885, 15, 1.06, 0.96),
(16387, 32780, 'Golfers', 716896, 0, 163886, 16, 1.06, 0.96),
(16387, 32780, 'High Property Sales', 716897, 0, 163887, 17, 1.06, 0.96),
(16387, 32780, 'Luxury Cars Owners', 716898, 0, 163888, 18, 1.06, 0.96),
(16387, 32780, 'Students', 716899, 0, 163889, 19, 1.06, 0.96),
(16387, 32780, 'Supermarket Shoppers', 716900, 0, 163890, 20, 1.06, 0.96);

insert into DMP_SELECTOR (DMP_VENDOR_ID, DMP_ATTRIBUTE_ID, NAME, MUID_SEGMENT_ID,
HIDDEN, EXTERNAL_ID, DISPLAY_ORDER, DATA_RETAIL, DATA_WHOLESALE)
values
(16387, 32780, 'BusinessCommuters', 1081454, 0, 21, 20, 1.06, 0.96),
(16387, 32780, 'BusinessTravellers', 1081455, 0, 22, 21, 1.06, 0.96),
(16387, 32780, 'NightLife', 1081456, 0, 23, 22, 1.06, 0.96),
(16387, 32780, 'BredLineFamilies', 1081457, 0, 24, 23, 1.06, 0.96),
(16387, 32780, 'WealthyRetirees', 1081458, 0, 25, 24, 1.06, 0.96),
(16387, 32780, 'StateDependant', 1081459, 0, 26, 25, 1.06, 0.96),
(16387, 32780, 'YoungCosmopoloitans', 1081460, 0, 27, 26, 1.06, 0.96),
(16387, 32780, 'ContentRetirees', 1081461, 0, 28, 27, 1.06, 0.96),
(16387, 32780, 'LowIncomeHouseHolds', 1081462, 0, 29, 28, 1.06, 0.96);

---
awk -F',' '{ print "call do_extract(150, " $1 ", " 716880 + $1 ");" }' < segments_taxonomy.csv

call do_extract(150, 1, 716881);
call do_extract(150, 2, 716882);
call do_extract(150, 3, 716883);
call do_extract(150, 4, 716884);
call do_extract(150, 5, 716885);
call do_extract(150, 6, 716886);
call do_extract(150, 7, 716887);
call do_extract(150, 8, 716888);
call do_extract(150, 9, 716889);
call do_extract(150, 10, 716890);
call do_extract(150, 11, 716891);
call do_extract(150, 12, 716892);
call do_extract(150, 13, 716893);
call do_extract(150, 14, 716894);
call do_extract(150, 15, 716895);
call do_extract(150, 16, 716896);
call do_extract(150, 17, 716897);
call do_extract(150, 18, 716898);
call do_extract(150, 19, 716899);
call do_extract(150, 20, 716900);

--

Calculating diffs:
time sort statiq-replace.tsv > sorted-new.tsv
time sort statiq-old-all.tsv > sorted-old.tsv
time diff --old-line-format='' --new-line-format='%L' --unchanged-line-format='' sorted-old.tsv sorted-new.tsv > diff-insert.tsv
time diff --old-line-format='%L' --new-line-format='' --unchanged-line-format='' sorted-old.tsv sorted-new.tsv > diff-delete.tsv

Load the diffs as sessions into MUID
(format is device_id, device_type_id, segment_id)

mysql --local-infile=1 -hlon3muidnodedb01 muid 
insert into inbound_session (inbound_process_id) values (4);
select last_insert_id() into @local_session_id;
select @local_session_id;
load data local infile '/tmp/diff-delete.tsv'
  into table inbound_load_device_segment
  set event_time = 0, inbound_session_id = @local_session_id,
  session_user_id = 59918, action_id = 2;
update inbound_session set status = 1 where inbound_session_id = @local_session_id;

insert into inbound_session (inbound_process_id) values (4);
select last_insert_id() into @local_session_id;
select @local_session_id;
load data local infile '/tmp/diff-insert.tsv'
  into table inbound_load_device_segment
  set event_time = 0, inbound_session_id = @local_session_id,
  session_user_id = 59918, action_id = 1;
update inbound_session set status = 1 where inbound_session_id = @local_session_id;






load data infile '/tmp/diff-insert.tsv'
  into table inbound_load_device_segment
  set event_time = 0, inbound_session_id = 2004192,
  session_user_id = 59918, action_id = 1;
update inbound_session set status = 1 where inbound_session_id = @local_session_id;
