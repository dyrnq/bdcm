ALTER TABLE public.artifact ADD COLUMN etag       varchar(512) DEFAULT NULL;
ALTER TABLE public.artifact ADD COLUMN file_size  BIGINT DEFAULT NULL;