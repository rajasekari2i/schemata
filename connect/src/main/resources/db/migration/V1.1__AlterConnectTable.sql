ALTER TABLE connect ADD channel_id bytea;
ALTER TABLE connect ALTER COLUMN user_email DROP NOT NULL;
ALTER TABLE connect ADD user_name character varying COLLATE pg_catalog."default";
ALTER TABLE connect add repo_organization character varying COLLATE pg_catalog."default";