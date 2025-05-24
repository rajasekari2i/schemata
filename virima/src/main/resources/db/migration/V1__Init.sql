create schema if not exists virima;

CREATE TABLE asset (
  id bigserial PRIMARY KEY,
  created_at timestamp,
  updated_at timestamp,
  created_by bigint,
  updated_by bigint,
  is_deleted boolean DEFAULT false,
  customer_id character varying COLLATE pg_catalog."default",
  blueprint character varying COLLATE pg_catalog."default",
  ip_address character varying COLLATE pg_catalog."default",
  asset_name character varying COLLATE pg_catalog."default",
  status character varying COLLATE pg_catalog."default",
  asset_id character varying COLLATE pg_catalog."default",
  host_name character varying COLLATE pg_catalog."default",
  operating_system character varying COLLATE pg_catalog."default",
  terminal_id character varying COLLATE pg_catalog."default",
  missing_components character varying COLLATE pg_catalog."default",
  hardware_asset character varying COLLATE pg_catalog."default"
);