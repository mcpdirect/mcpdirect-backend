-- aitool.aitool_property definition

-- Drop table

-- DROP TABLE aitool_property;

CREATE TABLE aitool_property (
	"key" varchar(100) NOT NULL,
	archived int8 DEFAULT 0 NOT NULL,
	value text NULL,
	"type" int4 DEFAULT 0 NULL,
	category varchar(100) NOT NULL,
	created int8 NULL,
	CONSTRAINT property_pkey PRIMARY KEY (key, archived)
);


-- aitool.app definition

-- Drop table

-- DROP TABLE app;

CREATE TABLE app (
	id int4 NOT NULL,
	package_id varchar(30) NOT NULL,
	"name" varchar(30) NOT NULL,
	created int8 NOT NULL,
	CONSTRAINT app_name_unique UNIQUE (name),
	CONSTRAINT app_pk PRIMARY KEY (id),
	CONSTRAINT app_unique UNIQUE (package_id)
);
CREATE INDEX app_created_idx ON aitool.app USING btree (created);


-- aitool.app_version definition

-- Drop table

-- DROP TABLE app_version;

CREATE TABLE app_version (
	app_id int4 NOT NULL,
	"version" varchar(20) NOT NULL,
	platform text NOT NULL,
	url varchar(100) NOT NULL,
	created int8 NOT NULL,
	arch varchar(10) NULL,
	CONSTRAINT app_version_pk PRIMARY KEY (app_id, version, platform)
);
CREATE INDEX app_version_app_id_idx ON aitool.app_version USING btree (app_id, platform);
CREATE INDEX app_version_created_idx ON aitool.app_version USING btree (created);


-- aitool.mcp_server_config definition

-- Drop table

-- DROP TABLE mcp_server_config;

CREATE TABLE mcp_server_config (
	id int8 NOT NULL,
	created int8 NOT NULL,
	url varchar NULL,
	command varchar NULL,
	args varchar NULL,
	env varchar NULL
);
CREATE INDEX mcp_server_config_created_idx ON aitool.mcp_server_config USING btree (created);


-- aitool.tool definition

-- Drop table

-- DROP TABLE tool;

CREATE TABLE tool (
	id int8 NOT NULL,
	maker_id int8 NOT NULL,
	status int2 NOT NULL,
	last_updated int8 NOT NULL,
	"name" varchar(100) NOT NULL,
	meta_data text NOT NULL,
	hash int4 NOT NULL,
	tags varchar DEFAULT '[]'::character varying NOT NULL,
	agent_id int8 DEFAULT 0 NOT NULL,
	agent_status int2 DEFAULT 0 NOT NULL,
	maker_status int2 DEFAULT 0 NOT NULL,
	CONSTRAINT tool_pk PRIMARY KEY (id),
	CONSTRAINT tool_unique UNIQUE (maker_id, name)
);
CREATE INDEX tool_agent_id_idx ON aitool.tool USING btree (agent_id);
CREATE INDEX tool_id_status_idx ON aitool.tool USING btree (id, status, agent_status, maker_status);
CREATE INDEX tool_last_updated_idx ON aitool.tool USING btree (last_updated);
CREATE INDEX tool_maker_id_idx ON aitool.tool USING btree (maker_id);
CREATE INDEX tool_status_idx ON aitool.tool USING btree (status);


-- aitool.tool_agent definition

-- Drop table

-- DROP TABLE tool_agent;

CREATE TABLE tool_agent (
	id int8 NOT NULL,
	user_id int8 NOT NULL,
	engine_id varchar(20) NOT NULL,
	app_id int8 DEFAULT 0 NOT NULL,
	created int8 NOT NULL,
	device varchar(200) NOT NULL,
	status int2 NOT NULL,
	"name" varchar(100) DEFAULT ''::character varying NOT NULL,
	tags varchar NULL,
	device_id int8 DEFAULT 0 NOT NULL,
	CONSTRAINT tools_agent_pk PRIMARY KEY (id),
	CONSTRAINT tools_agent_unique UNIQUE (user_id, engine_id)
);
CREATE INDEX tool_agent_user_id_idx ON aitool.tool_agent USING btree (user_id, device_id);
CREATE INDEX tools_agent_app_id_idx ON aitool.tool_agent USING btree (app_id);
CREATE INDEX tools_agent_created_idx ON aitool.tool_agent USING btree (created);


-- aitool.tool_app definition

-- Drop table

-- DROP TABLE tool_app;

CREATE TABLE tool_app (
	id int8 NOT NULL,
	"name" varchar(100) NOT NULL,
	description text NOT NULL,
	rating int2 DEFAULT 0 NOT NULL,
	summary varchar(200) DEFAULT ''::character varying NOT NULL,
	developer varchar(100) DEFAULT ''::character varying NOT NULL,
	"version" varchar(64) DEFAULT ''::character varying NOT NULL,
	CONSTRAINT tools_app_pk PRIMARY KEY (id),
	CONSTRAINT tools_app_unique UNIQUE (name)
);


-- aitool.tool_maker definition

-- Drop table

-- DROP TABLE tool_maker;

CREATE TABLE tool_maker (
	id int8 NOT NULL,
	created int8 NOT NULL,
	status int2 NOT NULL,
	"type" int4 NOT NULL,
	"name" varchar NOT NULL,
	tags varchar NULL,
	agent_id int8 NOT NULL,
	last_updated int8 NOT NULL,
	hash int8 DEFAULT 0 NOT NULL,
	tools varchar DEFAULT '[]'::character varying NOT NULL,
	CONSTRAINT tools_maker_unique UNIQUE (id)
);
CREATE INDEX tools_maker_agent_id_created_idx ON aitool.tool_maker USING btree (agent_id, created);
CREATE INDEX tools_maker_agent_id_idx ON aitool.tool_maker USING btree (agent_id);
CREATE UNIQUE INDEX tools_maker_agent_id_name_idx ON aitool.tool_maker USING btree (agent_id, name);
CREATE INDEX tools_maker_agent_id_status_idx ON aitool.tool_maker USING btree (agent_id, status);
CREATE INDEX tools_maker_agent_id_tyep_idx ON aitool.tool_maker USING btree (agent_id, type);
CREATE INDEX tools_maker_hash_idx ON aitool.tool_maker USING btree (hash);
CREATE INDEX tools_maker_last_updated_idx ON aitool.tool_maker USING btree (last_updated);


-- aitool.tool_permission definition

-- Drop table

-- DROP TABLE tool_permission;

CREATE TABLE tool_permission (
	user_id int8 NOT NULL,
	access_key_id int8 NOT NULL,
	tool_id int8 NOT NULL,
	last_updated int8 NOT NULL,
	status int2 NOT NULL,
	CONSTRAINT tool_permission_pk PRIMARY KEY (user_id, access_key_id, tool_id)
);
CREATE INDEX tool_permission_access_key_id_idx ON aitool.tool_permission USING btree (access_key_id);
CREATE INDEX tool_permission_last_updated_idx ON aitool.tool_permission USING btree (last_updated);
CREATE INDEX tool_permission_user_id_idx ON aitool.tool_permission USING btree (user_id);
CREATE INDEX tool_permission_user_id_key_id_idx ON aitool.tool_permission USING btree (user_id, access_key_id);
CREATE INDEX tool_permission_user_id_key_id_status_idx ON aitool.tool_permission USING btree (user_id, access_key_id, status);


-- aitool.tool_provider definition

-- Drop table

-- DROP TABLE tool_provider;

CREATE TABLE tool_provider (
	id int8 NOT NULL,
	user_id int8 NOT NULL,
	provider_id varchar NOT NULL,
	tools varchar NOT NULL,
	last_updated int8 NOT NULL,
	hash int8 DEFAULT 0 NOT NULL,
	app_id int8 DEFAULT 0 NOT NULL,
	device varchar DEFAULT '‘’'::character varying NOT NULL,
	CONSTRAINT tools_provider_pk PRIMARY KEY (id)
);
CREATE INDEX tools_provider_app_id_idx ON aitool.tool_provider USING btree (app_id);
CREATE INDEX tools_provider_last_updated_idx ON aitool.tool_provider USING btree (last_updated);
CREATE UNIQUE INDEX tools_provider_user_id_idx ON aitool.tool_provider USING btree (user_id, provider_id);