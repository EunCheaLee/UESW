/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.0.40 
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

create table `user` (
	`id` bigint (20) NOT NULL AUTO_INCREMENT PRIMARY KEY,,
	`account` varchar (765),
	`password` varchar (765),
	`email` varchar (765) NULL,
	`full_address` varchar (765),
	`phone_num` varchar (765),
	`user_name` varchar (765),
	`create_at` datetime ,
	`last_connect` datetime ,
	`birth` varchar (24)
); 
insert into `user` (`id`, `account`, `password`, `email`, `full_address`, `phone_num`, `user_name`, `create_at`, `last_connect`, `birth`) values('1','aaa','111','1@1.1','111-1111-1111','11111','111',NULL,NULL,'1111-11-11');
insert into `user` (`id`, `account`, `password`, `email`, `full_address`, `phone_num`, `user_name`, `create_at`, `last_connect`, `birth`) values('2','bbb','222','2@2.2','222-2222-2222','22222','222',NULL,NULL,'2000-12-12');
insert into `user` (`id`, `account`, `password`, `email`, `full_address`, `phone_num`, `user_name`, `create_at`, `last_connect`, `birth`) values('3','ccc','333','3@3.3','333-3333-3333','33333','333',NULL,NULL,'2003-03-03');
