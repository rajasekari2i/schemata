create schema if not exists analytics;
create extension pgcrypto;

CREATE TABLE task (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  task_type varchar(512) NOT NULL,
  service_type varchar(512) NOT NULL,
  connect_id bigint,
  url varchar(512),
  execution_interval bigint DEFAULT 3600000,
  last_sync_date timestamp without time zone
);

CREATE TABLE connect (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  service_type varchar(512) NOT NULL,
  auth_type varchar(512) NOT NULL,
  headers character varying COLLATE pg_catalog."default",
  project_key bytea,
  domain bytea,
  user_email bytea,
  auth_token bytea NOT NULL,
  refresh_token bytea,
  user_name character varying COLLATE pg_catalog."default",
  repo_organization character varying COLLATE pg_catalog."default",
  channel_id bytea;
);

CREATE TABLE sla (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  type character varying COLLATE pg_catalog."default",
  sla_time bigint
);

INSERT INTO sla(created_at, updated_at, created_by, updated_by, client_id, type, sla_time)
VALUES ('2022-03-19 00:00:00', '2022-03-19 00:00:00', 1, 1, 0, 'GITHUB', 86400);

CREATE TABLE client_repo
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  name character varying COLLATE pg_catalog."default",
  owner character varying COLLATE pg_catalog."default",
  full_name character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  default_branch character varying COLLATE pg_catalog."default",
  repo_type varchar(25) NOT NULL,
  repository_source character varying COLLATE pg_catalog."default",
  folder_path character varying COLLATE pg_catalog."default";
  connect_id bigint,
  CONSTRAINT client_repo_connect_id_fkey FOREIGN KEY (connect_id) REFERENCES connect(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE event_audit
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  client_name character varying COLLATE pg_catalog."default",
  type character varying COLLATE pg_catalog."default",
  event_id bigint NOT NULL,
  status character varying COLLATE pg_catalog."default",
  error character varying COLLATE pg_catalog."default",
  initiated_by character varying COLLATE pg_catalog."default"
);

CREATE TABLE domain 
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  client_repo_id bigint,
  name character varying COLLATE pg_catalog."default",
  node_id bigint NOT NULL,
  CONSTRAINT domain_client_repo_id_fkey FOREIGN KEY (client_repo_id) REFERENCES client_repo(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE workflow
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  status character varying COLLATE pg_catalog."default",
  domain_id bigint,
  node_id bigint,
  schema_name character varying COLLATE pg_catalog."default",
  stack_holders character varying COLLATE pg_catalog."default",
  purpose character varying COLLATE pg_catalog."default",
  creator character varying COLLATE pg_catalog."default",
  title character varying COLLATE pg_catalog."default",
  additional_reference character varying COLLATE pg_catalog."default",
  rank decimal,
  CONSTRAINT workflow_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE activity
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  workflow_id bigint NOT NULL,
  type character varying COLLATE pg_catalog."default",
  source_node_id bigint,
  target_node_id bigint,
  CONSTRAINT activity_workflow_id_fkey FOREIGN KEY (workflow_id) REFERENCES workflow(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
); 

CREATE TABLE pull_request
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  number character varying COLLATE pg_catalog."default" NOT NULL,
  workflow_id bigint,
  client_repo_id bigint NOT NULL,
  status character varying COLLATE pg_catalog."default",
  source_branch character varying COLLATE pg_catalog."default",
  target_branch character varying COLLATE pg_catalog."default",
  sha character varying COLLATE pg_catalog."default",
  url character varying COLLATE pg_catalog."default",
  validation_status varchar(10),
  error_message character varying COLLATE pg_catalog."default",
  issue_comment_id bigint,
  CONSTRAINT pull_request_workflow_id_fkey FOREIGN KEY (workflow_id) REFERENCES workflow(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT pull_request_client_repo_id_fkey FOREIGN KEY (client_repo_id) REFERENCES client_repo(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE comment
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  node_id bigint,
  comments character varying COLLATE pg_catalog."default",
  type character varying COLLATE pg_catalog."default",
  pull_request_id bigint NOT NULL,
  commentable_id bigint,
  is_resolved boolean,
  CONSTRAINT comment_pull_request_id  FOREIGN KEY (pull_request_id) REFERENCES pull_request(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT comment_commentable_id  FOREIGN KEY (commentable_id) REFERENCES comment(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE schema_file_audit
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  name character varying COLLATE pg_catalog."default",
  client_repo_id bigint,
  file_type character varying COLLATE pg_catalog."default",
  path character varying COLLATE pg_catalog."default",
  root_node_id bigint,
  pull_request_id bigint,
  checksum character varying COLLATE pg_catalog."default",
  CONSTRAINT schema_file_audit_client_repo_id_fkey FOREIGN KEY (client_repo_id) REFERENCES client_repo(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT schema_file_audit_pull_request_id_fkey FOREIGN KEY (pull_request_id) REFERENCES pull_request(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE model
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  type character varying COLLATE pg_catalog."default",
  name character varying COLLATE pg_catalog."default",
  name_space character varying COLLATE pg_catalog."default",
  path character varying COLLATE pg_catalog."default",
  node_id bigint NOT NULL,
  domain_id bigint NOT NULL,
  client_repo_id bigint,
  schema_file_audit_id bigint,
  pull_request_id bigint,
  checksum character varying COLLATE pg_catalog."default",
  CONSTRAINT model_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT model_client_repo_id_fkey FOREIGN KEY (client_repo_id) REFERENCES client_repo(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT model_schema_file_audit_id_fkey FOREIGN KEY (schema_file_audit_id) REFERENCES schema_file_audit(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT model_pull_request_id_fkey FOREIGN KEY (pull_request_id) REFERENCES pull_request(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
