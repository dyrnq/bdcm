ALTER TABLE `artifact` ADD COLUMN `etag`       varchar(512) DEFAULT NULL;
ALTER TABLE `artifact` ADD COLUMN `file_size`  BIGINT DEFAULT NULL;