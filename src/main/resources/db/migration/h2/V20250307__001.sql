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
  `_lock` INT DEFAULT NULL,
  `begin_lock` TIMESTAMP DEFAULT NULL ,
  `auto_job` INT DEFAULT NULL,
  `current_job_id` BIGINT DEFAULT NULL ,
  PRIMARY KEY (`id`)
);
CREATE INDEX idx_url ON `artifact` (url);

CREATE TABLE `art_job` (
  `id` BIGINT NOT NULL ,
  `art_id` BIGINT NOT NULL ,
  `user_id` varchar(40) NOT NULL ,
  `status` INT DEFAULT NULL,
  `begin_time` TIMESTAMP DEFAULT NULL ,
  `end_time` TIMESTAMP DEFAULT NULL ,
  `progress` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `art_job_log` (
  `id` BIGINT NOT NULL ,
  `art_job_id` BIGINT NOT NULL,
  `log` CLOB DEFAULT NULL ,
  PRIMARY KEY (`id`)
);

INSERT INTO `user` VALUES ('1', 'admin','hello@admin.com','13988888888', '$2a$12$nXPoohJkpNbD1oSxtN0P1uGxhYP40Rn1Z0Yh1yxQ2lMhdz2TOqIZu');
insert into `artifact` (`id`, `name`, `url`) values (686793170459835100, 'tomcat', 'https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.24/bin/apache-tomcat-10.1.24.tar.gz');
insert into `artifact` (`id`, `name`, `url`) values (686795975078542200, 'maven', 'https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594769, 'kube-apiserver', 'https://dl.k8s.io/v1.32.2/bin/linux/amd64/kube-apiserver');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594771, 'cfssl', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/cfssl_1.6.5_linux_amd64');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594772, 'cfssljson', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/cfssljson_1.6.5_linux_amd64');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594773, 'cfssl-bundle', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/cfssl-bundle_1.6.5_linux_amd64');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594774, 'cfssl-certinfo', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/cfssl-certinfo_1.6.5_linux_amd64');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594775, 'cfssl-newkey', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/cfssl-newkey_1.6.5_linux_amd64');
insert into `artifact` (`id`, `name`, `url`) values (686856938303594776, 'cfssl-scan', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/cfssl-scan_1.6.5_linux_amd64');
insert into `artifact` (`id`, `name`, `url`, `auto_job`) values (686856938303594777, 'mkbundle', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/mkbundle_1.6.5_linux_amd64',1);
insert into `artifact` (`id`, `name`, `url`, `auto_job`) values (686856938303594778, 'multirootca', 'https://github.com/cloudflare/cfssl/releases/download/v1.6.5/multirootca_1.6.5_linux_amd64',1);

insert into `artifact` (`id`, `name`, `url`, `auto_job`) values (686856938303594779, 'etcd', 'https://github.com/etcd-io/etcd/releases/download/v3.5.19/etcd-v3.5.19-linux-amd64.tar.gz',1);
insert into `artifact` (`id`, `name`, `url`, `auto_job`) values (686856938303594780, 'temurin21-binaries', 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6+7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.6_7.tar.gz',1);
insert into `artifact` (`id`, `name`, `url`, `auto_job`) values (686856938303594781, 'helm', 'https://get.helm.sh/helm-v3.17.2-linux-amd64.tar.gz',1);
insert into `artifact` (`id`, `name`, `url`, `auto_job`) values (686856938303594782, 'helm', 'https://get.helm.sh/helm-v3.17.2-linux-arm64.tar.gz',1);