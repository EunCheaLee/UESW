/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.0.40 : Database - mbcit
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `address` */

DROP TABLE IF EXISTS `address`;

CREATE TABLE `address` (
  `id` bigint NOT NULL,
  `street_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `full_address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `board` */

DROP TABLE IF EXISTS `board`;

CREATE TABLE `board` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext COLLATE utf8mb4_general_ci,
  `reply` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `view_num` int NOT NULL,
  `write_date` datetime(6) DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `user_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `last_connect` datetime(6) DEFAULT NULL,
  `reply_num` int NOT NULL,
  `notice` bit(1) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `depth` int NOT NULL,
  `refer` int NOT NULL,
  `step` int NOT NULL,
  `board_file` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `filename` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `message_content` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sender` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `type` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfyf1fchnby6hndhlfaidier1r` (`user_id`),
  CONSTRAINT `FKfyf1fchnby6hndhlfaidier1r` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `board_chk_1` CHECK ((`type` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `bus` */

DROP TABLE IF EXISTS `bus`;

CREATE TABLE `bus` (
  `bus_route_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `st_nm` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `lat` double DEFAULT NULL,
  `ars_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `chat_message` */

DROP TABLE IF EXISTS `chat_message`;

CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_content` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sender` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=131 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `flask` */

DROP TABLE IF EXISTS `flask`;

CREATE TABLE `flask` (
  `id` bigint NOT NULL,
  `부상자수` varbinary(255) DEFAULT NULL,
  `사고건수` varbinary(255) DEFAULT NULL,
  `사망자수` varbinary(255) DEFAULT NULL,
  `중상자수` varbinary(255) DEFAULT NULL,
  `road_types` varbinary(255) DEFAULT NULL,
  `years` varbinary(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Data for the table `flask` */

/*Table structure for table `location` */

DROP TABLE IF EXISTS `location`;

CREATE TABLE `location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `saved_at` datetime(6) DEFAULT NULL,
  `username` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `reply` */

DROP TABLE IF EXISTS `reply`;

CREATE TABLE `reply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `likes` int NOT NULL,
  `board_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `report_count` int NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `depth` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcs9hiip0bv9xxfrgoj0lwv2dt` (`board_id`),
  KEY `FKapyyxlgntertu5okpkr685ir9` (`user_id`),
  KEY `FK653lh98w5hv6139pt9s7ejyr8` (`parent_id`),
  CONSTRAINT `FK653lh98w5hv6139pt9s7ejyr8` FOREIGN KEY (`parent_id`) REFERENCES `reply` (`id`),
  CONSTRAINT `FKapyyxlgntertu5okpkr685ir9` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKcs9hiip0bv9xxfrgoj0lwv2dt` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `seoul` */

DROP TABLE IF EXISTS `seoul`;

CREATE TABLE `seoul` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `gu` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `region` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `subway` */

DROP TABLE IF EXISTS `subway`;

CREATE TABLE `subway` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `station_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `chosung` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=653 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `full_address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone_num` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `last_connect` datetime(6) DEFAULT NULL,
  `birth` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '0000',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '''비회원''',
  `account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `write_date` datetime(6) DEFAULT NULL,
  `profile_image_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `provider` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `provider_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''USER''',
  `street_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `address_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKddefmvbrws3hvl5t0hnnsv8ox` (`address_id`),
  CONSTRAINT `FKddefmvbrws3hvl5t0hnnsv8ox` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
