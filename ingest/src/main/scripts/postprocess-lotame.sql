-- Germany (15,743,764)
insert into device_country_type
  select device_id, 156, 6 from segment_device where segment_id = 35038;
-- UK (35,018,812)
insert into device_country_type
  select device_id, 150, 6 from segment_device where segment_id = 34792;
-- AU (3,939,377)
insert into device_country_type
  select device_id, 195, 6 from segment_device where segment_id = 37979;
-- Update for Android
update device_country_type dct
 inner join segment_device sd on sd.device_id = dct.device_id
 set device_type_id = 9
 where segment_id = 15661;
