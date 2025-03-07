CREATE TABLE `user` (
  `id` varchar(40) NOT NULL ,
  `name` varchar(40) DEFAULT NULL ,
  `email` varchar(256) DEFAULT NULL ,
  `phone` varchar(256) DEFAULT NULL ,
  `pass` varchar(512) DEFAULT NULL ,
  PRIMARY KEY (`id`)
);
INSERT INTO `user` VALUES ('1', 'admin','hello@admin.com','13988888888', '$2a$12$nXPoohJkpNbD1oSxtN0P1uGxhYP40Rn1Z0Yh1yxQ2lMhdz2TOqIZu');
