ALTER TABLE client_repo 
ADD COLUMN repository_source character varying COLLATE pg_catalog."default",
ADD COLUMN folder_path character varying COLLATE pg_catalog."default";