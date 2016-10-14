create database lotame;
use lotame;

CREATE TABLE `device` (
  `id` int(11) NOT NULL,
  `raw_did` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `raw_did` (`raw_did`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `device_country_type` (
  `device_id` int(11) NOT NULL,
  `country_id` int(11) NOT NULL DEFAULT '0',
  `device_type_id` int(11) DEFAULT '6',
  PRIMARY KEY (`device_id`,`country_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `extract` (
  `device_id` varchar(40) NOT NULL,
  `device_type_id` int(11) NOT NULL,
  `segment_id` int(11) NOT NULL,
  KEY `segment_id` (`segment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `segment` (
  `id` int(11) NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `level_1` varchar(255) NOT NULL,
  `level_2` varchar(255) DEFAULT NULL,
  `level_3` varchar(255) DEFAULT NULL,
  `level_4` varchar(255) DEFAULT NULL,
  `cpm` decimal(4,2) DEFAULT NULL,
  `definition` varchar(512) DEFAULT NULL,
  `muid_segment_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `segment_device` (
  `segment_id` int(11) NOT NULL,
  `device_id` int(11) NOT NULL,
  PRIMARY KEY (`segment_id`,`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `segment_population` (
  `segment_id` int(11) NOT NULL,
  `population` int(11) DEFAULT NULL,
  `last_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`segment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `do_extract`(IN in_country_id int, IN
in_segment_id int, IN in_output_id int)
begin
insert into extract
select
  if(device_type_id = 6, cast(sha1(upper(device.raw_did)) as char
  character set latin1), device.raw_did),
  if(device_type_id = 6, 7, device_type_id),
  in_output_id
from device_country_type dct
  inner join segment_device sd on sd.device_id = dct.device_id
  inner join device on device.id = dct.device_id
where segment_id = in_segment_id
  and country_id = in_country_id;
end
;;
delimiter ;

delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `proc_calc_segment_population`(in_segment_id INT)
BEGIN
  SET SESSION tx_isolation = 'READ-UNCOMMITTED';
  REPLACE INTO segment_population (segment_id, population)
    SELECT in_segment_id, count(*)
      FROM segment_device
      WHERE segment_id = in_segment_id; 
  SET SESSION tx_isolation = 'REPEATABLE-READ';
END
;;
delimiter ;

delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `proc_calc_all_segments_population`()
BEGIN
  DECLARE local_segment_id INT;
  DECLARE local_not_found INT(1) DEFAULT 0;
  DECLARE local_loop_complete INT(1) DEFAULT 0;
  DECLARE cur_ids CURSOR FOR SELECT id FROM segment ORDER BY id;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET local_not_found = 1;

  OPEN cur_ids;

  REPEAT
    FETCH cur_ids INTO local_segment_id;
    IF local_not_found = 1 THEN
      SET local_loop_complete = 1;
    ELSE
      CALL proc_calc_segment_population(local_segment_id);
    END IF;
  UNTIL local_loop_complete END REPEAT;

  CLOSE cur_ids;

END
;;
delimiter ;
