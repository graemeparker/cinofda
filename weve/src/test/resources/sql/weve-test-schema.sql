DROP SCHEMA weve;
CREATE SCHEMA weve;
SET SCHEMA weve;

DROP TABLE IF EXISTS DEVICE_IDENTIFIER_TYPE;
CREATE TABLE DEVICE_IDENTIFIER_TYPE
 (ID int(11) unsigned NOT NULL AUTO_INCREMENT,
  NAME varchar(255) NOT NULL,
  SYSTEM_NAME varchar(32) NOT NULL,
  PRECEDENCE_ORDER int(11) NOT NULL,
  HIDDEN tinyint(1) NOT NULL,
  VALIDATION_REGEX varchar(255) DEFAULT NULL,
  SECURE tinyint(1) NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY NAME (NAME),
  UNIQUE KEY SYSTEM_NAME (SYSTEM_NAME),
  UNIQUE KEY PRECEDENCE_ORDER (PRECEDENCE_ORDER));

INSERT INTO DEVICE_IDENTIFIER_TYPE VALUES (1,'DPID','dpid',100,0,'^[0-9A-Fa-f]{40}$',1),(2,'ODIN-1','odin-1',200,0,'^[0-9A-Fa-f]{40}$',1),(3,'OpenUDID','openudid',300,0,'^[0-9A-Fa-f]{40}$',1),(4,'Android Device ID','android',400,1,'^[0-9A-Fa-f]{16}$',0),(5,'UDID','udid',1000,1,'^[0-9A-Fa-f]{40}$',0),(6,'Apple''s Identifier For Advertising','ifa',500,1,'^([0-9A-Fa-f]{32}|[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12})$',0),(7,'Hashed IFA','hifa',499,0,'^[0-9A-Fa-f]{40}$',1),(8,'AdTruth Device Identifier','atid',99,1,'^[0-9A-Fa-f]{40}$',1);


DROP TABLE IF EXISTS decryption_method;
CREATE TABLE decryption_method (
  decryption_method_id int(10) unsigned NOT NULL DEFAULT '0',
  decryption_method_name varchar(255) DEFAULT 'No decryption',
  PRIMARY KEY (decryption_method_id)
);

INSERT INTO decryption_method VALUES (0,'No decryption');

DROP TABLE IF EXISTS muid_device_processing_queue;
CREATE TABLE muid_device_processing_queue (
  muid_device_processing_queue_id int(11) NOT NULL AUTO_INCREMENT,
  display_service_esk bigint(20) NOT NULL,
  device_id varchar(40) NOT NULL,
  device_identifier_type int(11) NOT NULL,
  inserted_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status int(11) unsigned NOT NULL DEFAULT '0',
  processed_at timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (muid_device_processing_queue_id)
);

DROP TABLE IF EXISTS service_user;
CREATE TABLE service_user (
  service_user_id int(11) unsigned NOT NULL DEFAULT '0',
  service_user_name varchar(255) DEFAULT NULL,
  PRIMARY KEY (service_user_id)
);

INSERT INTO service_user VALUES (1,'Telefonica'));

DROP TABLE IF EXISTS operator_ip_address_range;
CREATE TABLE operator_ip_address_range (
  ip_address_start bigint(11) unsigned NOT NULL DEFAULT '0',
  ip_address_end bigint(11) unsigned NOT NULL DEFAULT '0',
  service_user_id int(11) unsigned NOT NULL DEFAULT '0',
  decryption_method_id int(11) unsigned NOT NULL DEFAULT '0',
  header_name varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (ip_address_start,ip_address_end,service_user_id),
  CONSTRAINT fk_service_user_id FOREIGN KEY (service_user_id) REFERENCES service_user (service_user_id),
  CONSTRAINT operator_ip_address_range_ibfk_1 FOREIGN KEY (decryption_method_id) REFERENCES decryption_method (decryption_method_id)
);

INSERT INTO operator_ip_address_range VALUES (3368601601,3368601855,2,0,'x-weve-vf-id');

DROP TABLE IF EXISTS sync_service_master;
CREATE TABLE sync_service_master (
  display_uid varchar(64) NOT NULL,
  display_service_esk bigint(20) unsigned NOT NULL,
  service_user_id int(11) unsigned NOT NULL,
  consumer_type_id int(11) unsigned NOT NULL,
  contract_type_id int(11) unsigned NOT NULL,
  weve_device_type_id int(11) unsigned NOT NULL,
  permission_type_key_id int(11) unsigned NOT NULL,
  gender_id int(11) unsigned NOT NULL,
  birth_year int(11) unsigned NOT NULL,
  age_checked tinyint(1) unsigned NOT NULL,
  post_code_district varchar(20) DEFAULT NULL,
  end_user_address_type_id int(11) unsigned NOT NULL,
  data_usage_decile int(11) unsigned NOT NULL,
  gaming_usage_decile int(11) unsigned NOT NULL,
  sms_usage_decile int(11) unsigned NOT NULL,
  spend_usage_decile int(11) unsigned NOT NULL,
  voice_usage_decile int(11) unsigned NOT NULL,
  segment_key_id int(11) unsigned NOT NULL,
  segment_value float DEFAULT NULL,
  PRIMARY KEY (display_uid,service_user_id)
);

INSERT INTO sync_service_master VALUES ('adfonic',3425,2,0,0,0,0,0,0,0,'0',0,0,0,0,0,0,0,0),('adfonic1',2437472318,2,0,0,0,0,0,0,0,'0',0,0,0,0,0,0,0,0);