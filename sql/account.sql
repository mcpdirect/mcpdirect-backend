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
	usage_amount int4 DEFAULT 0 NOT NULL,
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


-- account.otp definition

-- Drop table

-- DROP TABLE otp;

CREATE TABLE otp (
	id int8 NOT NULL,
	otp varchar NOT NULL,
	expiration_date int8 NOT NULL,
	account varchar NOT NULL,
	CONSTRAINT otp_pk PRIMARY KEY (id)
);
CREATE INDEX otp_account_idx ON account.otp USING btree (account);
CREATE INDEX otp_expiration_date_idx ON account.otp USING btree (expiration_date);


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