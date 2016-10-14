DROP PROCEDURE IF EXISTS proc_check_weve_id_exists;
CREATE PROCEDURE proc_check_weve_id_exists(in_service_user_id int(11), in_display_uid varchar(64), OUT out_weve_id bigint(20))
BEGIN
	SET out_weve_id =0;
  SELECT display_service_esk into out_weve_id
    FROM sync_service_master
   WHERE service_user_id = in_service_user_id
     AND display_uid = in_display_uid;
END 

DROP PROCEDURE IF EXISTS proc_return_device_identifer_types;
CREATE PROCEDURE proc_return_device_identifer_types()
BEGIN
	 SELECT ID,
          NAME, 
          SYSTEM_NAME, 
          PRECEDENCE_ORDER, 
          HIDDEN, 
          VALIDATION_REGEX, 
          SECURE 
     FROM DEVICE_IDENTIFIER_TYPE;
END 

DROP PROCEDURE IF EXISTS proc_return_operator_ip_ranges;
CREATE PROCEDURE proc_return_operator_ip_ranges()
BEGIN
	SELECT  oidr.ip_address_start, 
          oidr.ip_address_end,
          oidr.service_user_id as service_user,
          oidr.decryption_method_id as decryption_method,
          oidr.header_name 
          FROM operator_ip_address_range oidr;
END 

DROP PROCEDURE IF EXISTS proc_store_device_ids;
CREATE PROCEDURE proc_store_device_ids(in_weve_id BIGINT(20), in_device_ids VARCHAR(1024))
BEGIN
	DECLARE local_device_ids VARCHAR(1024);
  DECLARE local_query VARCHAR(2048);
  
  set local_device_ids = replace(in_device_ids,"~","','");
  set local_device_ids = replace(local_device_ids,"|",CONCAT("',NULL,0,0),(NULL,",in_weve_id,",'"));
  
  set local_query = CONCAT ("INSERT INTO muid_device_processing_queue VALUES (NULL,",in_weve_id,",'",local_device_ids,"',NULL,0,0);");
  
  set @sql := local_query;
  PREPARE stmt FROM @sql;
  EXECUTE stmt;
  select ROW_COUNT() as 'record_inserted';
  DEALLOCATE PREPARE stmt;
    
END