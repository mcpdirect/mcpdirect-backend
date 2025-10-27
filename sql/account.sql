-- account.access_key definition

-- Drop table

-- DROP TABLE access_key;

CREATE TABLE access_key (
	id int8 NOT NULL,
	secret_key varchar(100) NOT NULL,
	effective_date int8 NOT NULL,
	expiration_date int8 NOT NULL,
	user_id int8 NOT NULL,
	user_roles int4 DEFAULT 0 NOT NULL,
	created int8 NOT NULL,
	status int2 NOT NULL,
	"name" varchar(50) DEFAULT ''::character varying NOT NULL,
	"usage" int4 DEFAULT 0 NOT NULL,
	CONSTRAINT access_key_pk PRIMARY KEY (user_id, id),
	CONSTRAINT access_key_unique UNIQUE (user_id, name)
);
CREATE INDEX access_key_effectivedata_idx ON account.access_key USING btree (effective_date);
CREATE INDEX access_key_expirationdate_idx ON account.access_key USING btree (expiration_date);
CREATE UNIQUE INDEX access_key_secret_key_idx ON account.access_key USING btree (secret_key);
CREATE INDEX access_key_userid_idx ON account.access_key USING btree (user_id);
CREATE INDEX access_key_usertype_idx ON account.access_key USING btree (user_roles);


-- account.account_property definition

-- Drop table

-- DROP TABLE account_property;

CREATE TABLE account_property (
	"key" varchar(100) NOT NULL,
	archived int8 DEFAULT 0 NOT NULL,
	value text NOT NULL,
	"type" int4 DEFAULT 0 NOT NULL,
	category varchar(100) NOT NULL,
	created int8 NOT NULL,
	CONSTRAINT property_pkey PRIMARY KEY (key, archived)
);


-- account."admin" definition

-- Drop table

-- DROP TABLE "admin";

CREATE TABLE "admin" (
	id int8 NOT NULL,
	nickname varchar(100) NOT NULL,
	created int8 NOT NULL,
	status int4 NOT NULL,
	CONSTRAINT admin_pk PRIMARY KEY (id),
	CONSTRAINT admin_unique UNIQUE (nickname)
);
CREATE UNIQUE INDEX admin_nn_idx ON account.admin USING btree (nickname) WITH (deduplicate_items='true');


-- account.admin_account definition

-- Drop table

-- DROP TABLE admin_account;

CREATE TABLE admin_account (
	id int8 NOT NULL,
	account varchar NOT NULL,
	"password" varchar NOT NULL,
	status int4 DEFAULT 0 NOT NULL,
	created int8 DEFAULT 0 NOT NULL,
	CONSTRAINT admin_account_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX admin_account_index ON account.admin_account USING btree (account) WITH (deduplicate_items='true');


-- account.otp definition

-- Drop table

-- DROP TABLE otp;

CREATE TABLE otp (
	id int8 NOT NULL,
	otp varchar NOT NULL,
	expiration_date int8 NOT NULL,
	account varchar NOT NULL,
	"type" int2 DEFAULT 0 NOT NULL,
	properties varchar NULL,
	CONSTRAINT otp_pk PRIMARY KEY (id)
);
CREATE INDEX otp_account_idx ON account.otp USING btree (account);
CREATE INDEX otp_expiration_date_idx ON account.otp USING btree (expiration_date);


-- account.service_provider definition

-- Drop table

-- DROP TABLE service_provider;

CREATE TABLE service_provider (
	id int8 NOT NULL,
	"name" varchar(100) NOT NULL,
	description varchar(500) NOT NULL,
	created int8 NOT NULL,
	status int4 NOT NULL,
	CONSTRAINT service_provider_pkey PRIMARY KEY (id)
);
CREATE INDEX sp_created ON account.service_provider USING btree (created) WITH (deduplicate_items='true');
CREATE UNIQUE INDEX sp_name_idx ON account.service_provider USING btree (name) WITH (deduplicate_items='true');
CREATE INDEX sp_status ON account.service_provider USING btree (status) WITH (deduplicate_items='true');


-- account.station definition

-- Drop table

-- DROP TABLE station;

CREATE TABLE station (
	id int8 NOT NULL,
	"name" varchar(100) NOT NULL,
	status int4 NOT NULL,
	created int8 NOT NULL,
	sp_id int8 NOT NULL,
	crypto_algo varchar(50) NOT NULL,
	engine_id varchar(30) NOT NULL,
	CONSTRAINT station_pkey PRIMARY KEY (id)
);
CREATE INDEX station_created_idx ON account.station USING btree (created) WITH (deduplicate_items='true');
CREATE INDEX station_crypto_algo ON account.station USING btree (crypto_algo) WITH (deduplicate_items='true');
CREATE INDEX station_engine_id ON account.station USING btree (engine_id) WITH (deduplicate_items='true');
CREATE INDEX station_name_idx ON account.station USING btree (name) INCLUDE (sp_id) WITH (deduplicate_items='true');
CREATE INDEX station_sp_id_idx ON account.station USING btree (sp_id) WITH (deduplicate_items='true');
CREATE INDEX station_status_idx ON account.station USING btree (status) WITH (deduplicate_items='true');


-- account.station_account definition

-- Drop table

-- DROP TABLE station_account;

CREATE TABLE station_account (
	id int8 NOT NULL,
	public_key varchar NOT NULL,
	machine_id int8 NOT NULL,
	CONSTRAINT station_account_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX sa_machine_id_idx ON account.station_account USING btree (machine_id) WITH (deduplicate_items='true');


-- account.team definition

-- Drop table

-- DROP TABLE team;

CREATE TABLE team (
	id int8 NOT NULL,
	"name" varchar(50) NOT NULL,
	created int8 NOT NULL,
	owner_id int8 NOT NULL,
	status int2 NOT NULL,
	last_updated int8 NOT NULL,
	CONSTRAINT team_pk PRIMARY KEY (id)
);
CREATE INDEX team_created_idx ON account.team USING btree (created);
CREATE INDEX team_name_idx ON account.team USING btree (name);
CREATE INDEX team_owner_id_idx ON account.team USING btree (owner_id);
CREATE UNIQUE INDEX team_owner_id_name_idx ON account.team USING btree (owner_id, name);
CREATE INDEX team_status_idx ON account.team USING btree (status);


-- account.team_member definition

-- Drop table

-- DROP TABLE team_member;

CREATE TABLE team_member (
	team_id int8 NOT NULL,
	member_id int8 NOT NULL,
	status int2 NOT NULL,
	created int8 NOT NULL,
	expiration_date int8 NOT NULL,
	last_updated int8 NOT NULL,
	CONSTRAINT team_member_pk PRIMARY KEY (team_id, member_id)
);
CREATE INDEX team_member_created_idx ON account.team_member USING btree (created);
CREATE INDEX team_member_expiration_date_idx ON account.team_member USING btree (expiration_date);
CREATE INDEX team_member_last_updated_idx ON account.team_member USING btree (last_updated);
CREATE INDEX team_member_member_id_idx ON account.team_member USING btree (member_id);
CREATE INDEX team_member_status_idx ON account.team_member USING btree (status);
CREATE INDEX team_member_team_id_idx ON account.team_member USING btree (team_id);


-- account."user" definition

-- Drop table

-- DROP TABLE "user";

CREATE TABLE "user" (
	id int8 NOT NULL,
	"name" varchar(100) NOT NULL,
	"type" int4 DEFAULT 0 NOT NULL,
	created int8 NOT NULL,
	"language" varchar(8) DEFAULT 'en-US'::character varying NOT NULL,
	CONSTRAINT user_pk PRIMARY KEY (id)
);
CREATE INDEX user_type_idx ON account."user" USING btree (type) WITH (deduplicate_items='true');


-- account.user_account definition

-- Drop table

-- DROP TABLE user_account;

CREATE TABLE user_account (
	id int8 NOT NULL,
	account varchar(64) NOT NULL,
	"password" varchar NOT NULL,
	status int4 DEFAULT 0 NOT NULL,
	key_seed varchar(32) DEFAULT ''::character varying NOT NULL,
	CONSTRAINT user_account_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX account_index ON account.user_account USING btree (account) WITH (deduplicate_items='true');


-- account.user_anonymous definition

-- Drop table

-- DROP TABLE user_anonymous;

CREATE TABLE user_anonymous (
	id int8 NOT NULL,
	secret_key varchar(64) NOT NULL,
	device_id varchar(64) NOT NULL,
	CONSTRAINT user_anonymous_pk PRIMARY KEY (id),
	CONSTRAINT user_anonymous_unique UNIQUE (device_id)
);

-- account.team definition

-- Drop table

-- DROP TABLE team;

CREATE TABLE team (
	id int8 NOT NULL,
	"name" varchar(50) NOT NULL,
	created int8 NOT NULL,
	owner_id int8 NOT NULL,
	CONSTRAINT team_pk PRIMARY KEY (id)
);
CREATE INDEX team_created_idx ON team (created);
CREATE INDEX team_name_idx ON team ("name");
CREATE INDEX team_owner_id_idx ON team (owner_id int8_ops);


-- account.team_member definition

-- Drop table

-- DROP TABLE team_member;

CREATE TABLE team_member (
	team_id int8 NOT NULL,
	member_id int8 NOT NULL,
	status int2 NOT NULL,
	created int8 NOT NULL,
	expiration_date int8 NOT NULL,
	CONSTRAINT team_member_pk PRIMARY KEY (team_id, member_id)
);
CREATE INDEX team_member_created_idx ON account.team_member USING btree (created);
CREATE INDEX team_member_expiration_date_idx ON account.team_member USING btree (expiration_date);
CREATE INDEX team_member_member_id_idx ON account.team_member USING btree (member_id);
CREATE INDEX team_member_status_idx ON account.team_member USING btree (status);