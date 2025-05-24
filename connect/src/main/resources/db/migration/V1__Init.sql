create schema if not exists analytics;
create extension pgcrypto;

CREATE TABLE incident (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  incident_id character varying COLLATE pg_catalog."default" NOT NULL UNIQUE,
  key character varying COLLATE pg_catalog."default",
  number int,
  incident_created_at timestamp,
  last_status_change_at timestamp,
  status character varying COLLATE pg_catalog."default",
  type character varying COLLATE pg_catalog."default",
  priority character varying COLLATE pg_catalog."default",
  urgency character varying COLLATE pg_catalog."default",
  title character varying COLLATE pg_catalog."default",
  description text COLLATE pg_catalog."default",
  summary text COLLATE pg_catalog."default",
  resolve_reason character varying COLLATE pg_catalog."default",
  html_url character varying COLLATE pg_catalog."default",
  service_id character varying COLLATE pg_catalog."default",
  service_type character varying COLLATE pg_catalog."default",
  service_summary text COLLATE pg_catalog."default",
  escalation_policy_id character varying COLLATE pg_catalog."default",
  escalation_policy_type character varying COLLATE pg_catalog."default",
  escalation_policy_summary text COLLATE pg_catalog."default",
  trigger_id character varying COLLATE pg_catalog."default",
  trigger_type character varying COLLATE pg_catalog."default",
  trigger_summary text COLLATE pg_catalog."default",
  alert_count_triggered integer,
  alert_count_resolved integer,
  alert_total_count integer,
  row_created_at timestamp without time zone,
  row_updated_at timestamp without time zone,
  row_deleted_at timestamp without time zone
);


CREATE TABLE incident_log_entry
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  entry_id character varying COLLATE pg_catalog."default" NOT NULL UNIQUE,
  entry_created_at timestamp,
  summary character varying COLLATE pg_catalog."default",
  description character varying COLLATE pg_catalog."default",
  type character varying COLLATE pg_catalog."default",
  html_url character varying COLLATE pg_catalog."default",
  agent_id character varying COLLATE pg_catalog."default",
  agent_type character varying COLLATE pg_catalog."default",
  agent_summary character varying COLLATE pg_catalog."default",
  channel_type character varying COLLATE pg_catalog."default",
  incident_id character varying COLLATE pg_catalog."default",
  incident_summary character varying COLLATE pg_catalog."default",
  incident_type character varying COLLATE pg_catalog."default",
  service_id character varying COLLATE pg_catalog."default",
  service_summary character varying COLLATE pg_catalog."default",
  service_type character varying COLLATE pg_catalog."default",
  row_created_at timestamp without time zone,
  row_updated_at timestamp without time zone,
  row_deleted_at timestamp without time zone
);

CREATE TABLE incident_metrics
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  incident_id character varying COLLATE pg_catalog."default" NOT NULL UNIQUE,
  team_id character varying COLLATE pg_catalog."default",
  service_id character varying COLLATE pg_catalog."default",
  metrics_created_at timestamp,
  resolved_at date,
  urgency character varying COLLATE pg_catalog."default",
  major boolean,
  priority_name character varying COLLATE pg_catalog."default",
  priority_order bigint,
  seconds_to_resolve bigint,
  seconds_to_first_ack bigint,
  seconds_to_engage bigint,
  seconds_to_mobilize bigint,
  engaged_seconds bigint,
  engaged_user_count bigint,
  escalation_count bigint,
  assignment_count bigint,
  business_hour_interruptions bigint,
  sleep_hour_interruptions bigint,
  off_hour_interruptions bigint,
  snoozed_seconds bigint
);

CREATE TABLE pillar
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  name character varying COLLATE pg_catalog."default" NOT NULL,
  description character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default"
);

CREATE TABLE cost_center
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  name character varying COLLATE pg_catalog."default" NOT NULL,
  description character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default"
);

CREATE TABLE team
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  name character varying COLLATE pg_catalog."default" NOT NULL,
  description character varying COLLATE pg_catalog."default",
  pillar_id bigint, -- foreign key to the pillar table
  team_channel character varying COLLATE pg_catalog."default",
  cost_center_id bigint, -- foreign key to the cost_center table
  parent_team_id bigint,
  manager_id bigint,
  status character varying COLLATE pg_catalog."default",
  CONSTRAINT team_cost_center_id_fkey FOREIGN KEY (cost_center_id) REFERENCES cost_center (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION, 
  CONSTRAINT team_pillar_id_fkey FOREIGN KEY (pillar_id) REFERENCES pillar (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE pager_duty_service
(
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  service_id varchar(512) NOT NULL UNIQUE,
  name character varying COLLATE pg_catalog."default" NOT NULL,
  description character varying COLLATE pg_catalog."default",
  data_tier character varying COLLATE pg_catalog."default",
  service_tier character varying COLLATE pg_catalog."default",
  alert_channel character varying COLLATE pg_catalog."default",
  team_id bigint,
  runbook character varying COLLATE pg_catalog."default",
  deploy_runbook character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  CONSTRAINT service_team_id_fkey FOREIGN KEY (team_id) REFERENCES team(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

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
  user_email bytea NOT NULL,
  auth_token bytea NOT NULL,
  refresh_token bytea
);

CREATE TABLE employee (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  name varchar(512) NOT NULL,
  designation varchar(512) NOT NULL,
  team_id bigint,
  team_name varchar(512) NOT NULL,
  manager_id bigint,
  manager_name varchar(512),
  manager_designation varchar(512),
  CONSTRAINT employee_team_id_fkey FOREIGN KEY (team_id) REFERENCES team(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT employee_manager_id_fkey FOREIGN KEY (manager_id) REFERENCES employee(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE askob_workspace (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  token character varying COLLATE pg_catalog."default",
  user_source_id character varying COLLATE pg_catalog."default" UNIQUE,
  key character varying COLLATE pg_catalog."default",
  name character varying COLLATE pg_catalog."default",
  type character varying COLLATE pg_catalog."default"
);

CREATE TABLE askob_routing (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  channel_origin character varying COLLATE pg_catalog."default" NOT NULL,
  channel_to character varying COLLATE pg_catalog."default" NOT NULL,
  origin_workspace_id bigint NOT NULL,
  to_workspace_id bigint NOT NULL,
  CONSTRAINT askob_routing_from_to_origin_workspace_id_from_works_key UNIQUE (channel_origin, channel_to, origin_workspace_id, to_workspace_id),
  CONSTRAINT askob_routing_origin_workspace_id_fkey FOREIGN KEY (origin_workspace_id) REFERENCES askob_workspace(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT askob_routing_to_workspace_id_fkey FOREIGN KEY (to_workspace_id) REFERENCES askob_workspace(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE askob_message (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  thread_ts character varying COLLATE pg_catalog."default" UNIQUE,
  message_ts character varying COLLATE pg_catalog."default" UNIQUE,
  type character varying COLLATE pg_catalog."default",
  message_user_id character varying COLLATE pg_catalog."default"
);

CREATE TABLE message_routing (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  from_message_id bigint NOT NULL,
  to_message_id bigint NOT NULL,
  from_workspace_id bigint NOT NULL,
  to_workspace_id bigint NOT NULL,
  from_channel character varying COLLATE pg_catalog."default",
  to_channel character varying COLLATE pg_catalog."default",
  CONSTRAINT message_routing_to_message_id_fkey FOREIGN KEY (to_message_id) REFERENCES askob_message(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT message_routing_from_message_id_fkey FOREIGN KEY (from_message_id) REFERENCES askob_message(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT message_routing_to_workspace_id_fkey FOREIGN KEY (to_workspace_id) REFERENCES askob_workspace(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
  CONSTRAINT message_routing_from_workspace_id_fkey FOREIGN KEY (from_workspace_id) REFERENCES askob_workspace(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE ticket_jira (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  ticket_id character varying COLLATE pg_catalog."default",
  key character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  priority character varying COLLATE pg_catalog."default",
  summary character varying COLLATE pg_catalog."default",
  description character varying COLLATE pg_catalog."default",
  issue_type character varying COLLATE pg_catalog."default",
  statuscategorychangedate character varying COLLATE pg_catalog."default",
  ticket_created timestamp,
  ticket_updated timestamp,
  assignee character varying COLLATE pg_catalog."default",
  creater character varying COLLATE pg_catalog."default",
  reporter character varying COLLATE pg_catalog."default",
  project_id character varying COLLATE pg_catalog."default",
  project_key character varying COLLATE pg_catalog."default",
  project_name character varying COLLATE pg_catalog."default"
);

CREATE TABLE ticket_zendesk (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  ticket_id character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  priority character varying COLLATE pg_catalog."default",
  summary character varying COLLATE pg_catalog."default",
  description character varying COLLATE pg_catalog."default",
  type character varying COLLATE pg_catalog."default",
  ticket_created timestamp,
  ticket_updated timestamp,
  requester_id character varying COLLATE pg_catalog."default",
  submitter_id character varying COLLATE pg_catalog."default",
  assignee_id character varying COLLATE pg_catalog."default",
  organization_id character varying COLLATE pg_catalog."default",
  group_id character varying COLLATE pg_catalog."default",
  is_public boolean,
  has_incidents boolean,
  due_at timestamp
);

CREATE TABLE ticket (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  ticket_created_by character varying COLLATE pg_catalog."default",
  ticket_updated_by character varying COLLATE pg_catalog."default",
  askob_message_id bigint,
  canonical_id character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  severity character varying COLLATE pg_catalog."default",
  assigned_to character varying COLLATE pg_catalog."default",
  title character varying COLLATE pg_catalog."default",
  description character varying COLLATE pg_catalog."default",
  CONSTRAINT ticket_askob_message_id_fkey FOREIGN KEY (askob_message_id) REFERENCES askob_message(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE ticket_action (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  ticket_id bigint NOT NULL,
  type character varying COLLATE pg_catalog."default",
  reference_id character varying COLLATE pg_catalog."default",
  CONSTRAINT ticket_action_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES ticket(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE ticket_audit (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  ticket_created_by character varying COLLATE pg_catalog."default",
  ticket_updated_by character varying COLLATE pg_catalog."default",
  ticket_id bigint NOT NULL,
  askob_message_id bigint,
  canonical_id character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  severity character varying COLLATE pg_catalog."default",
  assigned_to character varying COLLATE pg_catalog."default",
  title character varying COLLATE pg_catalog."default",
  description character varying COLLATE pg_catalog."default",
  CONSTRAINT ticket_log_entry_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES ticket(id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
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

CREATE TABLE metrics (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial NOT NULL,
  source character varying COLLATE pg_catalog."default" NOT NULL,
  source_id character varying COLLATE pg_catalog."default" NOT NULL,
  source_created_at timestamp,
  service_id character varying COLLATE pg_catalog."default",
  time_to_acknowledge bigint,
  first_reply_time bigint,
  time_to_resolve bigint,
  status character varying COLLATE pg_catalog."default"
);
