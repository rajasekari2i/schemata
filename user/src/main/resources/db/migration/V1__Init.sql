create schema if not exists ops_user;

CREATE TABLE client (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  name varchar(512) NOT NULL UNIQUE,
  description varchar(512) NOT NULL
);

-- INSERT INTO client(created_at, updated_at, created_by, updated_by, name, description)
-- VALUES ('2022-03-19 00:00:00', '2022-03-19 00:00:00', 1, 1, 'OPSBEACH', 'opsBeach');

CREATE TABLE "user" (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  client_id bigserial,
  dob date,
  email_id character varying(255),
  mobile character varying(255),
  first_name character varying(255),
  middle_name character varying(255),
  last_name character varying(255),
  username character varying(255),
  password character varying(255),
  forget_password_key character varying(255),
  gender character varying(50),
  type character varying(255),
  time_zone character varying(255),
  onboard_status character varying(255),
  failure_attempts integer,
  lock_time timestamp,
  account_locked boolean,
  verification_token_sent_time timestamp,
  password_changed_time timestamp,
  old_password character varying(500),
  otp character varying(6),
  otp_sent_time timestamp
);

-- INSERT INTO "user"(
--   email_id, username, mobile, first_name, client_id, onboard_status, created_at, updated_at, created_by, updated_by, forget_password_key, password, time_zone, failure_attempts, account_locked, password_changed_time)
--   VALUES ('schematalabs@gmail.com', 'schematalabs@gmail.com', '1234567891', 'Admin', 1, 'COMPLETED', '2022-03-17 00:00:00', '2022-03-19 00:00:00', 1, 1, null, '$2a$10$zo7hGv0u1MNBcpDLWhfageHpHMj.JW.chuU05xy8VdhSOaGc7iNKa', 'Asia/Kolkata', 0, FALSE, null);

CREATE TABLE role (
  id bigserial PRIMARY KEY,
  name varchar(512) NOT NULL,
  description varchar(512) NOT NULL,
  is_deleted boolean,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

CREATE TABLE permission (
  id bigserial PRIMARY KEY,
  operation varchar(256) NOT NULL,
  is_deleted boolean,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

CREATE TABLE role_permission (
  id bigserial PRIMARY KEY,
  role_id bigint NOT NULL,
  permission_id bigint NOT NULL,
  is_deleted boolean,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

CREATE TABLE user_role (
  id bigserial PRIMARY KEY,
  role_id bigint NOT NULL,
  user_id bigint NOT NULL,
  is_deleted boolean,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

INSERT INTO role (name, description, created_at, updated_at, created_by, updated_by, is_deleted)
    VALUES ('ORG_ADMIN', 'Admin Role', '2022-04-02 00:00:00', '2021-04-02 00:00:00', 0, 0, FALSE),
    ('SERVICE_ADMIN', 'Client Admin Role', '2022-04-02 00:00:00', '2021-04-02 00:00:00', 0, 0, FALSE),
    ('ACCOUNT_ADMIN', 'Client User Role', '2022-06-04 00:00:00', '2021-04-02 00:00:00', 0, 0, FALSE),
    ('USER', 'Client User Role', '2022-06-04 00:00:00', '2021-04-02 00:00:00', 0, 0, FALSE);

-- INSERT INTO user_role (role_id, user_id, created_at, updated_at, created_by, updated_by, is_deleted)
--     VALUES ((SELECT id FROM "ops_user"."role" WHERE name = 'USER'), (SELECT id FROM "ops_user"."user" WHERE username = 'schematalabs@gmail.com'), '2022-04-02 00:00:00', '2021-04-02 00:00:00', 0, 0, FALSE);

CREATE TABLE system_configuration (
  id bigserial PRIMARY KEY,
  key varchar(256) NOT NULL,
  value varchar(256) NOT NULL,
  data_type varchar(50) NOT NULL,
  description varchar(256) NOT NULL,
  client_id bigserial,
  is_deleted boolean,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

CREATE TABLE jwt (
  id bigserial PRIMARY KEY,
  public_key text NOT NULL,
  private_key text NOT NULL,
  access_token text NOT NULL,
  refresh_token text NOT NULL,
  is_deleted boolean,
  expiry_at timestamp,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

CREATE TABLE session (
  id bigserial PRIMARY KEY,
  user_id bigserial,
  uri varchar(512) NOT NULL,
  type varchar(512) NOT NULL,
  action varchar(512) NOT NULL,
  module varchar(512) NOT NULL,
  ip_address varchar(512) NOT NULL,
  success_login boolean,
  is_deleted boolean,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint
);

ALTER TABLE client ADD is_onboarded boolean DEFAULT false;
ALTER TABLE client ALTER COLUMN description DROP NOT NULL;