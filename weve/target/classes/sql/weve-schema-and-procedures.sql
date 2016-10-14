drop database if exists weve;
create database weve;
use weve;
-- MySQL dump 10.13  Distrib 5.5.24, for Linux (x86_64)
--
-- Host: localhost    Database: weve
-- ------------------------------------------------------
-- Server version	5.5.24-55

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `DEVICE_IDENTIFIER_TYPE`
--

DROP TABLE IF EXISTS `DEVICE_IDENTIFIER_TYPE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEVICE_IDENTIFIER_TYPE` (
  `ID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `SYSTEM_NAME` varchar(32) NOT NULL,
  `PRECEDENCE_ORDER` int(11) NOT NULL,
  `HIDDEN` tinyint(1) NOT NULL,
  `VALIDATION_REGEX` varchar(255) DEFAULT NULL,
  `SECURE` tinyint(1) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME` (`NAME`),
  UNIQUE KEY `SYSTEM_NAME` (`SYSTEM_NAME`),
  UNIQUE KEY `PRECEDENCE_ORDER` (`PRECEDENCE_ORDER`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DEVICE_IDENTIFIER_TYPE`
--

LOCK TABLES `DEVICE_IDENTIFIER_TYPE` WRITE;
/*!40000 ALTER TABLE `DEVICE_IDENTIFIER_TYPE` DISABLE KEYS */;
INSERT INTO `DEVICE_IDENTIFIER_TYPE` VALUES (1,'DPID','dpid',100,0,'^[0-9A-Fa-f]{40}$',1),(2,'ODIN-1','odin-1',200,0,'^[0-9A-Fa-f]{40}$',1),(3,'OpenUDID','openudid',300,0,'^[0-9A-Fa-f]{40}$',1),(4,'Android Device ID','android',400,1,'^[0-9A-Fa-f]{16}$',0),(5,'UDID','udid',1000,1,'^[0-9A-Fa-f]{40}$',0),(6,'Apple\'s Identifier For Advertising','ifa',500,1,'^([0-9A-Fa-f]{32}|[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12})$',0),(7,'Hashed IFA','hifa',499,0,'^[0-9A-Fa-f]{40}$',1),(8,'AdTruth Device Identifier','atid',99,1,'^[0-9A-Fa-f]{40}$',1);
/*!40000 ALTER TABLE `DEVICE_IDENTIFIER_TYPE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `decryption_method`
--

DROP TABLE IF EXISTS `decryption_method`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `decryption_method` (
  `decryption_method_id` int(10) unsigned NOT NULL DEFAULT '0',
  `decryption_method_name` varchar(255) DEFAULT 'No decryption',
  PRIMARY KEY (`decryption_method_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `decryption_method`
--

LOCK TABLES `decryption_method` WRITE;
/*!40000 ALTER TABLE `decryption_method` DISABLE KEYS */;
INSERT INTO `decryption_method` VALUES (0,'No decryption');
/*!40000 ALTER TABLE `decryption_method` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `muid_device_processing_queue`
--

DROP TABLE IF EXISTS `muid_device_processing_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `muid_device_processing_queue` (
  `muid_device_processing_queue_id` int(11) NOT NULL AUTO_INCREMENT,
  `display_service_esk` bigint(20) NOT NULL,
  `device_id` varchar(40) NOT NULL,
  `device_identifier_type` int(11) NOT NULL,
  `inserted_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int(11) unsigned NOT NULL DEFAULT '0',
  `processed_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`muid_device_processing_queue_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `muid_device_processing_queue`
--

LOCK TABLES `muid_device_processing_queue` WRITE;
/*!40000 ALTER TABLE `muid_device_processing_queue` DISABLE KEYS */;
INSERT INTO `muid_device_processing_queue` VALUES (1,46573653,'37d1635361202a4109521a752b1c319fc6cfc74f',7,'2013-09-25 11:56:05',0,'0000-00-00 00:00:00'),(2,46573653,'ace78e2610cb56e3ea81db9b87ff77fba8c03368',1,'2013-09-25 11:56:05',0,'0000-00-00 00:00:00'),(3,7686687,'37d1635361202a4109521a752b1c319fc6cfc74f',7,'2013-10-01 08:46:24',0,'0000-00-00 00:00:00'),(4,7686687,'ace78e2610cb56e3ea81db9b87ff77fba8c03368',1,'2013-10-01 08:46:24',0,'0000-00-00 00:00:00');
/*!40000 ALTER TABLE `muid_device_processing_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `muid_option_processing_queue`
--

DROP TABLE IF EXISTS `muid_option_processing_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `muid_option_processing_queue` (
  `muid_device_processing_queue_id` int(11) NOT NULL AUTO_INCREMENT,
  `device_id` varchar(40) NOT NULL,
  `device_identifier_type` int(11) NOT NULL,
  `option` int(11) DEFAULT NULL,
  `inserted_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int(11) unsigned NOT NULL DEFAULT '0',
  `processed_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`muid_device_processing_queue_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `muid_option_processing_queue`
--

LOCK TABLES `muid_option_processing_queue` WRITE;
/*!40000 ALTER TABLE `muid_option_processing_queue` DISABLE KEYS */;
INSERT INTO `muid_option_processing_queue` VALUES (1,'37d1635361202a4109521a752b1c319fc6cfc74f',7,1,'2013-10-01 11:49:31',0,'0000-00-00 00:00:00'),(2,'ace78e2610cb56e3ea81db9b87ff77fba8c03368',1,1,'2013-10-01 11:49:31',0,'0000-00-00 00:00:00');
/*!40000 ALTER TABLE `muid_option_processing_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operator_ip_address_range`
--

DROP TABLE IF EXISTS `operator_ip_address_range`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `operator_ip_address_range` (
  `ip_address_start` int(11) unsigned NOT NULL DEFAULT '0',
  `ip_address_end` int(11) unsigned NOT NULL DEFAULT '0',
  `service_user_id` int(11) unsigned NOT NULL DEFAULT '0',
  `decryption_method_id` int(11) unsigned NOT NULL DEFAULT '0',
  `header_name` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`ip_address_start`,`ip_address_end`,`service_user_id`),
  KEY `fk_service_user_id` (`service_user_id`),
  KEY `operator_ip_address_range_ibfk_1` (`decryption_method_id`),
  CONSTRAINT `fk_service_user_id` FOREIGN KEY (`service_user_id`) REFERENCES `service_user` (`service_user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `operator_ip_address_range_ibfk_1` FOREIGN KEY (`decryption_method_id`) REFERENCES `decryption_method` (`decryption_method_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operator_ip_address_range`
--

LOCK TABLES `operator_ip_address_range` WRITE;
/*!40000 ALTER TABLE `operator_ip_address_range` DISABLE KEYS */;
INSERT INTO `operator_ip_address_range` VALUES (3368601601,3368601855,2,0,'x-weve-vf-id');
/*!40000 ALTER TABLE `operator_ip_address_range` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_user`
--

DROP TABLE IF EXISTS `service_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_user` (
  `service_user_id` int(11) unsigned NOT NULL DEFAULT '0',
  `service_user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`service_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_user`
--

LOCK TABLES `service_user` WRITE;
/*!40000 ALTER TABLE `service_user` DISABLE KEYS */;
INSERT INTO `service_user` VALUES (1,'Telefonica'));
/*!40000 ALTER TABLE `service_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sync_service_master`
--

DROP TABLE IF EXISTS `sync_service_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sync_service_master` (
  `display_uid` varchar(64) NOT NULL,
  `display_service_esk` bigint(20) unsigned NOT NULL,
  `service_user_id` int(11) unsigned NOT NULL,
  `consumer_type_id` int(11) unsigned NOT NULL,
  `contract_type_id` int(11) unsigned NOT NULL,
  `weve_device_type_id` int(11) unsigned NOT NULL,
  `permission_type_key_id` int(11) unsigned NOT NULL,
  `gender_id` int(11) unsigned NOT NULL,
  `birth_year` int(11) unsigned NOT NULL,
  `age_checked` tinyint(1) unsigned NOT NULL,
  `post_code_district` varchar(20) DEFAULT NULL,
  `end_user_address_type_id` int(11) unsigned NOT NULL,
  `data_usage_decile` int(11) unsigned NOT NULL,
  `gaming_usage_decile` int(11) unsigned NOT NULL,
  `sms_usage_decile` int(11) unsigned NOT NULL,
  `spend_usage_decile` int(11) unsigned NOT NULL,
  `voice_usage_decile` int(11) unsigned NOT NULL,
  `segment_key_id` int(11) unsigned NOT NULL,
  `segment_value` float DEFAULT NULL,
  PRIMARY KEY (`display_uid`,`service_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sync_service_master`
--

LOCK TABLES `sync_service_master` WRITE;
/*!40000 ALTER TABLE `sync_service_master` DISABLE KEYS */;
INSERT INTO `sync_service_master` VALUES ('adfonic',3425,2,0,0,0,0,0,0,0,'0',0,0,0,0,0,0,0,0),('adfonic1',2437472318,2,0,0,0,0,0,0,0,'0',0,0,0,0,0,0,0,0);
/*!40000 ALTER TABLE `sync_service_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'weve'
--
/*!50003 DROP PROCEDURE IF EXISTS `proc_check_weve_id_exists` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50020 DEFINER=`int_beacon_srvce`@`localhost`*/ /*!50003 PROCEDURE `proc_check_weve_id_exists`(in_service_user_id int(11), in_display_uid varchar(64), OUT out_weve_id bigint(20))
BEGIN
	SET out_weve_id =0;
  SELECT display_service_esk into out_weve_id
    FROM sync_service_master
   WHERE service_user_id = in_service_user_id
     AND display_uid = in_display_uid;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `proc_opt_change_device_ids` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50020 DEFINER=`int_opt_srvce`@`localhost`*/ /*!50003 PROCEDURE `proc_opt_change_device_ids`(in_device_ids VARCHAR(1024),in_option INT(11))
BEGIN
	DECLARE local_device_ids VARCHAR(1024);
  DECLARE local_query VARCHAR(2048);
  IF RIGHT(in_device_ids,1) = '|' THEN
    select -3;
  ELSEIF LOCATE('~',in_device_ids) = 0 OR LOCATE('~',in_device_ids) > LOCATE('|',in_device_ids) THEN
    SELECT -1;
  ELSEIF LOCATE('~',in_device_ids) !=41 THEN
    SELECT -2;
  ELSE
    set local_device_ids = replace(in_device_ids,"~","','");
    set local_device_ids = replace(local_device_ids,"|",CONCAT("',",in_option,",NULL,0,0),(NULL,'"));
    
    set local_query = CONCAT ("INSERT INTO muid_option_processing_queue VALUES (NULL,'",local_device_ids,"',",in_option,",NULL,0,0);");

    set @sql := local_query;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    select ROW_COUNT() as 'record_inserted';
    DEALLOCATE PREPARE stmt;
  END IF;  
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `proc_return_device_identifer_types` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50020 DEFINER=`int_beacon_srvce`@`localhost`*/ /*!50003 PROCEDURE `proc_return_device_identifer_types`()
BEGIN
	 SELECT ID,
          NAME, 
          SYSTEM_NAME, 
          PRECEDENCE_ORDER, 
          HIDDEN, 
          VALIDATION_REGEX, 
          SECURE 
     FROM DEVICE_IDENTIFIER_TYPE;
     
  select USER();
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `proc_return_operator_ip_ranges` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50020 DEFINER=`int_beacon_srvce`@`localhost`*/ /*!50003 PROCEDURE `proc_return_operator_ip_ranges`()
BEGIN
	SELECT  oidr.ip_address_start, 
          oidr.ip_address_end,
          oidr.service_user_id as service_user,
          oidr.decryption_method_id as decryption_method,
          oidr.header_name 
          FROM operator_ip_address_range oidr;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `proc_store_device_ids` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50020 DEFINER=`int_beacon_srvce`@`localhost`*/ /*!50003 PROCEDURE `proc_store_device_ids`(in_weve_id BIGINT(20), in_device_ids VARCHAR(1024))
BEGIN
	DECLARE local_device_ids VARCHAR(1024);
  DECLARE local_query VARCHAR(2048);
  IF RIGHT(in_device_ids,1) = '|' THEN
    select -3;
  ELSEIF LOCATE('~',in_device_ids) = 0 OR LOCATE('~',in_device_ids) > LOCATE('|',in_device_ids) THEN
    SELECT -1;
  ELSEIF LOCATE('~',in_device_ids) !=41 THEN
    SELECT -2;
  ELSE
    set local_device_ids = replace(in_device_ids,"~","','");
    set local_device_ids = replace(local_device_ids,"|",CONCAT("',NULL,0,0),(NULL,",in_weve_id,",'"));
    
    set local_query = CONCAT ("INSERT INTO muid_device_processing_queue VALUES (NULL,",in_weve_id,",'",local_device_ids,"',NULL,0,0);");

    set @sql := local_query;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    select ROW_COUNT() as 'record_inserted';
    DEALLOCATE PREPARE stmt;
  END IF;  
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-10-01 13:10:57
GRANT USAGE ON *.* TO 'int_beacon_srvce'@'localhost' IDENTIFIED BY PASSWORD '*B14B2797E745DFA7A7B463E37C37F520E03EE1AF';
DROP USER 'int_beacon_srvce'@'localhost';
GRANT USAGE ON *.* TO 'int_beacon_srvce'@'localhost' IDENTIFIED BY PASSWORD '*B14B2797E745DFA7A7B463E37C37F520E03EE1AF';
GRANT EXECUTE ON weve.* TO 'int_beacon_srvce'@'localhost';
GRANT SELECT ON weve.operator_ip_address_range TO 'int_beacon_srvce'@'localhost';
GRANT SELECT ON weve.sync_service_master TO 'int_beacon_srvce'@'localhost';
GRANT SELECT ON weve.DEVICE_IDENTIFIER_TYPE TO 'int_beacon_srvce'@'localhost';
GRANT INSERT ON weve.muid_device_processing_queue TO 'int_beacon_srvce'@'localhost';
GRANT USAGE ON *.* TO 'int_opt_srvce'@'localhost' IDENTIFIED BY PASSWORD '*B14B2797E745DFA7A7B463E37C37F520E03EE1AF';
DROP USER 'int_opt_srvce'@'localhost';
GRANT USAGE ON *.* TO 'int_opt_srvce'@'localhost' IDENTIFIED BY PASSWORD '*B14B2797E745DFA7A7B463E37C37F520E03EE1AF';
GRANT EXECUTE ON weve.* TO 'int_opt_srvce'@'localhost';
GRANT SELECT ON weve.operator_ip_address_range TO 'int_opt_srvce'@'localhost';
GRANT SELECT ON weve.sync_service_master TO 'int_opt_srvce'@'localhost';
GRANT SELECT ON weve.DEVICE_IDENTIFIER_TYPE TO 'int_opt_srvce'@'localhost';
GRANT INSERT ON weve.muid_device_processing_queue TO 'int_opt_srvce'@'localhost';
GRANT INSERT ON weve.muid_option_processing_queue TO 'int_opt_srvce'@'localhost';
