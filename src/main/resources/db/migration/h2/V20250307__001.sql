CREATE TABLE `user` (
  `id` varchar(40) NOT NULL ,
  `name` varchar(40) DEFAULT NULL ,
  `email` varchar(256) DEFAULT NULL ,
  `phone` varchar(256) DEFAULT NULL ,
  `pass` varchar(512) DEFAULT NULL ,
  PRIMARY KEY (`id`)
);

CREATE TABLE `artifact` (
  `id` BIGINT NOT NULL ,
  `name` varchar(256) DEFAULT NULL ,
  `url` varchar(2048) DEFAULT NULL ,
  `insert_time` TIMESTAMP DEFAULT NULL ,
  `update_time` TIMESTAMP DEFAULT NULL ,
  `final_status` INT DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `user` VALUES ('1', 'admin','hello@admin.com','13988888888', '$2a$12$nXPoohJkpNbD1oSxtN0P1uGxhYP40Rn1Z0Yh1yxQ2lMhdz2TOqIZu');
insert into `artifact` (`id`, `name`, `url`) values (686793170459835100, 'tomcat', 'https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.24/bin/apache-tomcat-10.1.24.tar.gz');
insert into `artifact` (`id`, `name`, `url`) values (686795975078542200, 'maven', 'https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594769, 'kube-apiserver', 'https://dl.k8s.io/v1.32.2/bin/linux/amd64/kube-apiserver');
