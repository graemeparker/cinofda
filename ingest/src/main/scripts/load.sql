set unique_checks=0;
set innodb_flush_log_at_trx_commit=0;
SET SESSION tx_isolation='READ-UNCOMMITTED';
select now() as StartTime;
load data local infile 'device.out' ignore into table device;
select now() as DeviceLoadComplete;
load data local infile 'segments.out' ignore into table segment_device;
select now() as SegmentLoadComplete;
