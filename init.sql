-- Create public tables 

-- public.qs_audit_events definition

-- Drop table

-- DROP TABLE public.qs_audit_events;
--CREATE DATABASE IF NOT EXISTS devqsai;
--USE devqsai;

CREATE TABLE public.qs_audit_events (
	audit_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	event_type varchar(50) NULL,
	event_type_action varchar(50) NULL,
	audit_message varchar(500) NULL,
	notification_message varchar(500) NULL,
	user_id int4 NULL,
	project_id int4 NULL,
	user_name varchar(100) NULL,
	creation_date timestamp NULL,
	is_notify bool NULL DEFAULT false,
	active bool NULL DEFAULT true,
	is_notify_read bool NULL DEFAULT false,
	CONSTRAINT qs_audit_events_pkey PRIMARY KEY (audit_id)
);



-- public.qs_datasource_types definition

-- Drop table

-- DROP TABLE public.qs_datasource_types;

CREATE TABLE public.qs_datasource_types (
	data_source_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	data_source_type varchar(20) NOT NULL,
	data_source_name varchar(60) NOT NULL,
	data_source_image varchar(60) NULL,
	active bool NULL,
	creation_date timestamp NULL
);


-- public.qs_metadata_reference definition

-- Drop table

-- DROP TABLE public.qs_metadata_reference;

CREATE TABLE public.qs_metadata_reference (
	ref_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	source_columnname_type varchar(50) NOT NULL,
	destination_columnname_type varchar(50) NOT NULL,
	source_type varchar(50) NOT NULL,
	target_type varchar(50) NOT NULL,
	active bool NOT NULL,
	created_by int2 NULL,
	created_date timestamp NULL,
	modified_by int2 NULL,
	modified_date timestamp NULL,
	CONSTRAINT qs_metadata_reference_pk PRIMARY KEY (ref_id)
);
-- public.qs_notifications definition

-- Drop table

-- DROP TABLE public.qs_notifications;

CREATE TABLE public.qs_notifications (
	notification_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	notification_msg varchar(512) NULL,
	admin_msg bool NULL,
	notification_read bool NULL,
	creation_date timestamp NULL,
	project_id int4 NULL DEFAULT '-1'::integer,
	user_id int4 NULL DEFAULT '-1'::integer,
	CONSTRAINT qs_notifications_pkey PRIMARY KEY (notification_id)
);


-- public.qs_product_features definition

-- Drop table

-- DROP TABLE public.qs_product_features;

CREATE TABLE public.qs_product_features (
	id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	"module" varchar(100) NULL,
	feature varchar(712) NULL,
	active bool NULL,
	subscription_type varchar(20) NULL,
	creation_date timestamp NULL,
	modified_date timestamp NULL,
	CONSTRAINT qs_features_explored_pkey PRIMARY KEY (id)
);
-- public.qs_project definition

-- Drop table

-- DROP TABLE public.qs_project;

CREATE TABLE public.qs_project (
	project_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	bucket_name varchar(255) NULL,
	created_date timestamp NULL,
	db_schema_name varchar(255) NULL,
	org_name varchar(255) NULL,
	processed_crawler varchar(255) NULL,
	processed_db varchar(255) NULL,
	project_automate varchar(255) NULL,
	project_dataset varchar(255) NULL,
	project_desc varchar(255) NULL,
	project_engineer varchar(255) NULL,
	project_name varchar(255) NULL,
	project_outcome varchar(255) NULL,
	raw_crawler varchar(255) NULL,
	raw_db varchar(255) NULL,
	user_id int4 NULL,
	project_display_name varchar(255) NULL,
	created_by varchar(100) NULL,
	modified_by varchar(100) NULL,
	modified_date timestamp NULL,
	active bool NULL DEFAULT true,
	eng_crawler varchar(255) NULL,
	eng_db varchar(255) NULL,
	project_logo varchar(500) NULL,
	is_deleted bool NULL DEFAULT false,
	deletion_date timestamp NULL,
	mark_as_default bool NULL DEFAULT false
);

-- public.qs_project_cumulative_size_info definition

-- Drop table

-- DROP TABLE public.qs_project_cumulative_size_info;

CREATE TABLE public.qs_project_cumulative_size_info (
	id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	project_id int4 NULL,
	user_id int4 NULL,
	cumulative_size int8 NULL,
	CONSTRAINT qs_project_cumulative_size_info_pkey PRIMARY KEY (id)
);
-- public.qs_project_subscription_details definition

-- Drop table

-- DROP TABLE public.qs_project_subscription_details;

CREATE TABLE public.qs_project_subscription_details (
	project_subscription_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	project_id int4 NOT NULL,
	user_id int4 NOT NULL,
	subscription_type varchar(100) NOT NULL,
	subscription_plan_type varchar(100) NOT NULL,
	subscription_plan_type_id int4 NOT NULL,
	subscription_valid_from timestamp NULL,
	subscription_valid_to timestamp NULL,
	subscription_id varchar(100) NULL,
	invoice_id varchar(100) NULL,
	charge_status varchar(50) NULL,
	subscription_status varchar(50) NULL,
	creation_date timestamp NULL,
	active bool NULL DEFAULT false,
	sub_total float8 NULL,
	tax float8 NULL,
	total float8 NULL,
	CONSTRAINT qs_project_subscription_details_pk PRIMARY KEY (project_subscription_id)
);


-- public.qs_rules_catalog definition

-- Drop table

-- DROP TABLE public.qs_rules_catalog;

CREATE TABLE public.qs_rules_catalog (
	rule_def_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	created_by varchar(255) NULL,
	created_date timestamp NULL,
	modified_by varchar(255) NULL,
	modified_date timestamp NULL,
	rule_contents text NULL,
	rule_name text NULL,
	user_id int4 NOT NULL,
	CONSTRAINT qs_rules_catalog_rule_name_key UNIQUE (rule_name),
	CONSTRAINT uk_82wk7n21mgx9of211kxo9qcvm UNIQUE (rule_name)
);

-- public.qs_subscription_v2 definition

-- Drop table

-- DROP TABLE public.qs_subscription_v2;

CREATE TABLE public.qs_subscription_v2 (
	subscription_id int4 NULL,
	"name" varchar(30) NULL,
	plan_type varchar(30) NULL,
	plan_type_price float8 NULL,
	accounts int4 NULL,
	plan_type_account_price float8 NULL,
	validity_days int4 NULL,
	plan_type_id varchar(100) NULL,
	plan_type_currency varchar(30) NULL,
	plan_type_settings text NULL,
	active bool NULL,
	sub_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	CONSTRAINT qs_subscription_v2_pkey PRIMARY KEY (sub_id)
);

-- public.qs_udf definition

-- Drop table

-- DROP TABLE public.qs_udf;

CREATE TABLE public.qs_udf (
	udf_id serial NOT NULL,
	active bool NOT NULL,
	arguments text NULL,
	created_by varchar(255) NULL,
	created_date timestamp NULL,
	modified_by varchar(255) NULL,
	modified_date timestamp NULL,
	project_id int4 NOT NULL,
	udf_filepath varchar(255) NULL,
	udf_iconpath varchar(255) NULL,
	udf_name varchar(255) NULL,
	udf_publish bool NOT NULL,
	udf_returnvalue varchar(255) NULL,
	udf_scriptlanguage varchar(255) NULL,
	udf_syntax varchar(255) NULL,
	udf_version varchar(255) NULL,
	user_id int4 NOT NULL,
	CONSTRAINT qs_udf_pkey PRIMARY KEY (udf_id)
);


-- public.qs_user_v2 definition

-- Drop table

-- DROP TABLE public.qs_user_v2;

CREATE TABLE public.qs_user_v2 (
	user_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	user_email varchar(100) NULL,
	user_pwd varchar(25) NOT NULL,
	salt varchar(255) NOT NULL,
	user_role varchar(15) NULL,
	user_parent_id int4 NOT NULL,
	user_subscription_type_id int4 NULL,
	user_subscription_valid_from timestamp NULL,
	user_subscription_valid_to timestamp NULL,
	user_default_projects int4 NULL,
	user_redash_key varchar(100) NULL,
	active bool NULL,
	first_time_login bool NULL DEFAULT true,
	user_subscription_type varchar(20) NULL,
	failed_login_attempts_count int4 NULL DEFAULT 0,
	modified_date timestamp NULL,
	stripe_customer_id varchar(100) NULL,
	user_subscription_plan_type varchar(50) NULL,
	user_type varchar(100) NOT NULL DEFAULT 'qsai'::character varying,
	CONSTRAINT qs_user1_pkey PRIMARY KEY (user_id)
);


-- public.qs_userprofile definition

-- Drop table

-- DROP TABLE public.qs_userprofile;

CREATE TABLE public.qs_userprofile (
	user_id int4 NOT NULL,
	user_first_name varchar(50) NULL,
	user_last_name varchar(50) NULL,
	user_middle_name varchar(50) NULL,
	user_company varchar(50) NULL,
	user_depart varchar(50) NULL,
	user_phone varchar(15) NULL,
	user_country varchar(50) NULL,
	creation_date timestamp NULL,
	created_by varchar(50) NULL,
	modified_by varchar(50) NULL,
	modified_date timestamp NULL,
	user_image varchar(500) NULL,
	user_company_role varchar(50) NULL
);
CREATE INDEX "fki_FK_UserId" ON public.qs_userprofile USING btree (user_id);


-- public.qs_userprofile foreign keys

ALTER TABLE public.qs_userprofile ADD CONSTRAINT "FK_UserId" FOREIGN KEY (user_id) REFERENCES qs_user_v2(user_id);


-- public.qs_userprojects definition

-- Drop table

-- DROP TABLE public.qs_userprojects;

CREATE TABLE public.qs_userprojects (
	id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	project_id int4 NOT NULL,
	user_id int4 NOT NULL,
	creation_date timestamp NULL,
	created_by varchar(100) NULL,
	modified_date timestamp NULL,
	modified_by varchar(100) NULL
);


CREATE TABLE public.qs_qsai_customer_info (
	customer_id serial NOT NULL,
	user_id int4 NOT NULL,
	stripe_customer_identifier varchar(255) NULL,
	created_date timestamp NULL,
	modified_date timestamp NULL,
	CONSTRAINT qsai_customer_info_pkey PRIMARY KEY (customer_id)
);

-- public.qs_redash_view_info definition

-- Drop table

-- DROP TABLE public.qs_redash_view_info;

CREATE TABLE public.qs_redash_view_info (
	redash_view_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	schema_name varchar(50) NULL,
	view_name varchar(100) NULL,
	active bool NULL,
	creation_date timestamp NULL
);


-- Triggers

-- FUNCTION: public.log_cleanse_job_changes()

-- DROP FUNCTION IF EXISTS public.log_cleanse_job_changes();

CREATE OR REPLACE FUNCTION public.log_cleanse_job_changes()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
DECLARE
   user_name_tmp varchar;
   file_name_tmp varchar;
   final_table_name varchar;
   audit_message varchar;
   notification_message varchar;
BEGIN
   --SELECT db_schema_name FROM public.qs_project where project_id=new.project_id into db_schema_name_tmp;
   SELECT concat(user_first_name,' ',user_last_name) as user_name FROM public.qs_userprofile WHERE USER_ID=new.user_id INTO user_name_tmp;
   
   final_table_name := TG_TABLE_SCHEMA||'.'||'qsp_file';
   EXECUTE (format('SELECT file_name FROM %s where file_id=%s;', final_table_name, new.file_id)) INTO file_name_tmp;
   
   if new.status != 'RUNNING' then
    
	audit_message := format('<span style="color:#00DFBA;">%s </span><span style="color:#f6bd5d;">Cleanse Job on File:</span> %s', new.status, file_name_tmp);
	notification_message := new.status || ' '|| 'Cleanse Job on File: '||file_name_tmp;
	
   	INSERT INTO public.qs_audit_events
   (event_type, event_type_action, audit_message, notification_message, user_id, project_id, 
   user_name, creation_date, is_notify, active, is_notify_read)
   VALUES('Cleanse Job', new.status, audit_message, notification_message, new.user_id, 
		    new.project_id, user_name_tmp, now(), true, true, false);
		
    END IF;	
		
   return new;
END;
$BODY$;

ALTER FUNCTION public.log_cleanse_job_changes()
    OWNER TO postgres;


-- FUNCTION: public.log_eng_job_changes()

-- DROP FUNCTION IF EXISTS public.log_eng_job_changes();

CREATE OR REPLACE FUNCTION public.log_eng_job_changes()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
DECLARE
   user_id_tmp integer;
   project_id_tmp integer;
   user_name_tmp varchar;
   eng_flow_name_tmp varchar;
   qsp_eng_flow_job_tbl_tmp varchar;
   qsp_eng_flow_tbl_tmp varchar;
   audit_message varchar;
   notification_message varchar;
BEGIN
   qsp_eng_flow_job_tbl_tmp := TG_TABLE_SCHEMA||'.'||'qsp_eng_flow_job';
   qsp_eng_flow_tbl_tmp := TG_TABLE_SCHEMA||'.'||'qsp_eng_flow';
   
   EXECUTE (format('SELECT eng_flow_name FROM %s where eng_flow_id=%s;', qsp_eng_flow_tbl_tmp, new.eng_flow_id)) INTO eng_flow_name_tmp;
   EXECUTE (format('SELECT user_id FROM %s where eng_flow_id=%s;', qsp_eng_flow_tbl_tmp, new.eng_flow_id)) INTO user_id_tmp;
   EXECUTE (format('SELECT project_id FROM %s where eng_flow_id=%s;', qsp_eng_flow_tbl_tmp, new.eng_flow_id)) INTO project_id_tmp;
   
   SELECT concat(user_first_name,' ',user_last_name) as user_name FROM public.qs_userprofile WHERE USER_ID=user_id_tmp INTO user_name_tmp;
   
   if new.status != 'RUNNING' then
    
	audit_message := format('<span style="color:#00DFBA;">%s </span><span style="color:#f6bd5d;">Engineering Job: </span> %s triggered.', new.status, eng_flow_name_tmp);
	notification_message := new.status || ' '|| 'Engineering Job: '||eng_flow_name_tmp|| ' triggered.';
	
   	INSERT INTO public.qs_audit_events
   (event_type, event_type_action, audit_message, notification_message, user_id, project_id, 
   user_name, creation_date, is_notify, active, is_notify_read)
   VALUES('Engineering Job', new.status, audit_message, notification_message, user_id_tmp, 
		    project_id_tmp, user_name_tmp, now(), true, true, false);
		
    END IF;	
		
   return new;
END;
$BODY$;

ALTER FUNCTION public.log_eng_job_changes()
    OWNER TO postgres;



--Procedure to create project tables

-- PROCEDURE: public.create_project_schema_and_tables(character varying, integer)

-- DROP PROCEDURE IF EXISTS public.create_project_schema_and_tables(character varying, integer);

CREATE OR REPLACE PROCEDURE public.create_project_schema_and_tables(
	schemaname character varying,
	INOUT result integer)
LANGUAGE 'plpgsql'
AS $BODY$

BEGIN

	EXECUTE 'CREATE SCHEMA ' || schemaName;
	EXECUTE 'ALTER SCHEMA '||schemaName|| ' OWNER TO postgres';

	-- 
	-- TOC entry 3892 (class 0 OID 0)
	-- Dependencies: 7
	-- Name: SCHEMA schemaName; Type: COMMENT; Schema: -; Owner: postgres
	--
	-- EXECUTE 'COMMENT ON SCHEMA '|| schemaName || ' IS '||schemaName;

	SET default_tablespace = '';
	SET default_table_access_method = heap;

	--
	-- TOC entry 504 (class 1259 OID 179111)
	-- Name: qsp_datadictionary; Type: TABLE; Schema: schemaName; Owner: postgres
	--

	--- Table creation started..

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_cleansing_param
	(
	    cleansing_param_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    cleansing_rule_id integer NOT NULL,
	    file_id integer NOT NULL,
	    folder_id integer NOT NULL,
	    rule_delimiter character varying(255) COLLATE pg_catalog."default",
	    --rule_impacted_col1 character varying(255) COLLATE pg_catalog."default",
	    --rule_impacted_col2 character varying(255) COLLATE pg_catalog."default",
	    --rule_impacted_col3 character varying(255) COLLATE pg_catalog."default",
	    --rule_impacted_col4 character varying(255) COLLATE pg_catalog."default",
	    --rule_impacted_col5 character varying(255) COLLATE pg_catalog."default",
	    rule_impacted_cols text COLLATE pg_catalog."default",
	    rule_input_logic character varying(255) COLLATE pg_catalog."default",
	    rule_input_logic1 character varying(255) COLLATE pg_catalog."default",
	    rule_input_logic2 character varying(255) COLLATE pg_catalog."default",
	    rule_input_logic3 character varying(255) COLLATE pg_catalog."default",
	    rule_input_logic4 character varying(255) COLLATE pg_catalog."default",
	    rule_input_logic5 character varying(255) COLLATE pg_catalog."default",
	    rule_input_values character varying(255) COLLATE pg_catalog."default",
	    rule_input_values1 character varying(255) COLLATE pg_catalog."default",
	    rule_input_values2 character varying(255) COLLATE pg_catalog."default",
	    rule_input_values3 character varying(255) COLLATE pg_catalog."default",
	    rule_input_values4 character varying(255) COLLATE pg_catalog."default",
	    rule_input_values5 character varying(255) COLLATE pg_catalog."default",
	    rule_output_values character varying(255) COLLATE pg_catalog."default",
	    rule_sequence integer NOT NULL,
	    parent_rule_ids character varying(500) COLLATE pg_catalog."default",
	    creation_date timestamp without time zone,
	    modified_date timestamp without time zone,
	    rule_input_newcolumns text COLLATE pg_catalog."default",
	    active boolean default true,
	    CONSTRAINT qsp_cleansing_param_pkey PRIMARY KEY (cleansing_param_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_cleansing_rule
	(
	    cleansing_rule_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    folder_id integer NOT NULL,
	    rule_name character varying(255) COLLATE pg_catalog."default",
	    rule_tech_desc character varying(255) COLLATE pg_catalog."default",
	    sequence integer NOT NULL,
	    CONSTRAINT qsp_cleansing_rule_pkey PRIMARY KEY (cleansing_rule_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_datadictionary
	(
	    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    file_id integer NOT NULL,
	    folder_id integer NOT NULL,
	    column_name character varying(100) COLLATE pg_catalog."default",
	    data_type character varying(100) COLLATE pg_catalog."default",
	    description character varying(255) COLLATE pg_catalog."default",
	    example character varying(512) COLLATE pg_catalog."default",
	    regular_expression character varying(100) COLLATE pg_catalog."default",
	    data_custodian character varying(500) COLLATE pg_catalog."default",
	    tags character varying(255) COLLATE pg_catalog."default",
	    published boolean,
	    created_by character varying(100) COLLATE pg_catalog."default",
	    creation_date timestamp without time zone,
	    modified_by character varying(100) COLLATE pg_catalog."default",
            modified_date timestamp without time zone,
            active boolean DEFAULT true,
            regex_type character varying(2),
            pd_regex_code integer default 0
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_dataset_reference
	(
	    data_set_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    athena_table character varying(255) COLLATE pg_catalog."default",
	    file_id integer NOT NULL,
	    file_name character varying(255) COLLATE pg_catalog."default",
	    folder_id integer NOT NULL,
	    folder_name character varying(255) COLLATE pg_catalog."default",
	    project_id character varying(255) COLLATE pg_catalog."default",
	    table_partition character varying(255) COLLATE pg_catalog."default",
	    created_by timestamp without time zone,
	    CONSTRAINT qsp_dataset_reference_pkey PRIMARY KEY (data_set_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_datasources_info
	(
	    data_source_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    connection_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
	    type character varying(50) COLLATE pg_catalog."default" NOT NULL,
	    project_id integer,
	    host character varying(100) COLLATE pg_catalog."default",
	    port character varying(10) COLLATE pg_catalog."default",
	    schema_name character varying(50) COLLATE pg_catalog."default",
	    user_name character varying(255) COLLATE pg_catalog."default",
	    password character varying(255) COLLATE pg_catalog."default",
	    creation_date timestamp without time zone,
	    created_by character varying(255) COLLATE pg_catalog."default",
	    CONSTRAINT qsp_datasources_info_pkey PRIMARY KEY (data_source_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_eng_flow
	(
	    eng_flow_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    modified_date timestamp without time zone,
	    created_date timestamp without time zone,
	    eng_flow_desc character varying(255) COLLATE pg_catalog."default",
	    eng_flow_meta_data text COLLATE pg_catalog."default",
	    eng_flow_name character varying(255) COLLATE pg_catalog."default",
	    active boolean NOT NULL,
	    parent_eng_flow_id integer NOT NULL,
	    project_id integer NOT NULL,
	    user_id integer NOT NULL,
	    created_by character varying(255) COLLATE pg_catalog."default",
	    eng_flow_config text COLLATE pg_catalog."default",
		eng_flow_display_name varchar(100) NULL,
	    CONSTRAINT qsp_eng_flow_pkey PRIMARY KEY (eng_flow_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_eng_flow_event
	(
	    eng_flow_event_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 500 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    eng_flow_config text COLLATE pg_catalog."default",
	    eng_flow_event_data text COLLATE pg_catalog."default",
	    eng_flow_id integer NOT NULL,
	    eng_flows3location character varying(255) COLLATE pg_catalog."default",
	    event_type character varying(50) COLLATE pg_catalog."default",
	    file_meta_data text COLLATE pg_catalog."default",
	    join_operations text COLLATE pg_catalog."default",
	    event_status boolean DEFAULT false,
	    auto_config_event_id integer,
	    event_progress double precision,
	    livy_stmt_execution_status character varying(15) COLLATE pg_catalog."default",
	    livy_stmt_output text COLLATE pg_catalog."default",
	    livy_stmt_start_time timestamp without time zone,
	    livy_stmt_end_time timestamp without time zone,
            livy_stmt_duration integer DEFAULT 0,
            project_id integer DEFAULT 0,
	    folder_id integer default 0,
	    file_id integer default 0,
	    user_id integer default 0,
            creation_date timestamp without time zone,
            file_type character varying(50),
            deleted boolean default false, 
	    CONSTRAINT qsp_eng_flow_event_pkey PRIMARY KEY (eng_flow_event_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_eng_flow_job
	(
	    flow_job_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    eng_flow_id integer NOT NULL,
	    status character varying(255) COLLATE pg_catalog."default",
	    rundate timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	    batch_job_id integer DEFAULT -1,
	    batch_job_log text COLLATE pg_catalog."default",
            batch_job_duration bigint DEFAULT 0,
            autorun_req_payload text,
	    CONSTRAINT qsp_eng_flow_job_pkey PRIMARY KEY (flow_job_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_eng_graph
	(
	    eng_graph_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    content text COLLATE pg_catalog."default",
	    eng_flow_id integer NOT NULL,
	    project_id integer NOT NULL,
	    user_id integer NOT NULL,
	    CONSTRAINT qsp_eng_graph_pkey PRIMARY KEY (eng_graph_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_engflow_metadata_awsref
	(
	    eng_aws_ref_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    athena_table character varying(255) COLLATE pg_catalog."default",
	    created_by character varying(255) COLLATE pg_catalog."default",
	    created_date timestamp without time zone,
	    eng_flow_id integer NOT NULL,
	    eng_flow_meta_data text COLLATE pg_catalog."default",
	    flow_type character varying(255) COLLATE pg_catalog."default",
	    table_partition character varying(255) COLLATE pg_catalog."default",
	    CONSTRAINT qsp_engflow_metadata_awsref_pkey PRIMARY KEY (eng_aws_ref_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_engg
	(
	    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    content text COLLATE pg_catalog."default",
	    project_id integer NOT NULL,
	    user_id integer NOT NULL,
	    CONSTRAINT qsp_engg_pkey PRIMARY KEY (id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_file
	(
	    file_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    modified_date timestamp without time zone,
	    created_date timestamp without time zone,
	    file_name character varying(255) COLLATE pg_catalog."default",
	    file_size character varying(255) COLLATE pg_catalog."default",
	    file_type character varying(255) COLLATE pg_catalog."default",
	    file_version_num integer NOT NULL,
	    folder_id integer NOT NULL,
	    project_id integer NOT NULL,
	    qs_meta_data text COLLATE pg_catalog."default",
	    rule_created_from character varying(255) COLLATE pg_catalog."default",
	    user_id integer NOT NULL,
	    CONSTRAINT qsp_file_pkey PRIMARY KEY (file_id),
	    active boolean DEFAULT true,
	    additional_info text
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_filemetadata_awsref
	(
	    aws_ref_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    athena_table character varying(255) COLLATE pg_catalog."default",
	    created_by character varying(255) COLLATE pg_catalog."default",
	    created_date timestamp without time zone,
	    file_id integer NOT NULL,
	    file_meta_data text COLLATE pg_catalog."default",
	    file_type character varying(255) COLLATE pg_catalog."default",
	    table_partition character varying(255) COLLATE pg_catalog."default",
	    CONSTRAINT qsp_filemetadata_awsref_pkey PRIMARY KEY (aws_ref_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_folder
	(
	    folder_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    cleansing_rule_set_name character varying(255) COLLATE pg_catalog."default",
	    created_by character varying(255) COLLATE pg_catalog."default",
	    created_date timestamp without time zone,
	    data_owner character varying(255) COLLATE pg_catalog."default",
	    data_prep_freq character varying(255) COLLATE pg_catalog."default",
	    file_id integer NOT NULL,
	    folder_desc character varying(255) COLLATE pg_catalog."default",
	    folder_name character varying(255) COLLATE pg_catalog."default",
		folder_display_name character varying(255) COLLATE pg_catalog."default",
	    modified_by character varying(255) COLLATE pg_catalog."default",
	    modified_date timestamp without time zone,
	    project_id integer NOT NULL,
	    user_id integer NOT NULL,
	    is_external boolean,
	    parent_id integer,
	    column_headers text COLLATE pg_catalog."default",
	    CONSTRAINT qsp_folder_pkey PRIMARY KEY (folder_id),
	    active boolean DEFAULT true
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_partition
	(
	    partition_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    file_id integer NOT NULL,
	    file_name character varying(255) COLLATE pg_catalog."default",
	    folder_id integer NOT NULL,
	    partition_name character varying(255) COLLATE pg_catalog."default",
	    project_id integer NOT NULL,
	    s3location character varying(255) COLLATE pg_catalog."default",
	    folder_name character varying(255) COLLATE pg_catalog."default",
	    CONSTRAINT qsp_partition_pkey PRIMARY KEY (partition_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_run_job_status
	(
	    run_job_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    created_date timestamp without time zone,
	    aws_create_job_id character varying(255) COLLATE pg_catalog."default",
	    aws_run_job_id character varying(255) COLLATE pg_catalog."default",
	    status character varying(255) COLLATE pg_catalog."default" NOT NULL,
	    user_id integer NOT NULL,
	    project_id integer NOT NULL,
	    file_id character varying(255) COLLATE pg_catalog."default",
	    folder_id integer,
	    batch_job_id integer DEFAULT -1,
            batch_job_log text COLLATE pg_catalog."default",
            batch_job_duration bigint DEFAULT 0,
	    CONSTRAINT qs_run_job_status_pkey PRIMARY KEY (run_job_id)
	)';

	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_udf
	(
	    udf_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    project_id integer NOT NULL,
	    udf_name character varying(60) COLLATE pg_catalog."default" NOT NULL,
	    udf_info text COLLATE pg_catalog."default",
	    active boolean,
	    created_date timestamp without time zone,
	    modified_date timestamp without time zone,
	    created_by character varying(100) COLLATE pg_catalog."default",
	    modified_by character varying(100) COLLATE pg_catalog."default",
	    arguments text COLLATE pg_catalog."default"
	)';
	
	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_redash_view_info (
	    redash_view_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    schema_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
	    view_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
	    active boolean,
	    creation_date timestamp without time zone
	)';
	
	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_file_data_profiling_info
	(
	    data_profile_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
	    project_id integer,
	    folder_id integer,
	    file_id integer,
	    file_profiling_info text COLLATE pg_catalog."default",
	    stats_type character varying(30) COLLATE pg_catalog."default",
	    active boolean DEFAULT true,
	    creation_date timestamp without time zone,
	    modified_date timestamp without time zone,
	    CONSTRAINT qsp_file_data_profiling_info_pkey PRIMARY KEY (data_profile_id)
	)';
	
	EXECUTE '
	CREATE TABLE '||schemaName||'.qsp_eng_flow_datalineage
	(
	    lineage_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY (INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1),
	    eng_flow_id integer,
	    event_id integer,
	    event_mapping_id integer,
	    event_type character varying(6) COLLATE pg_catalog."default",
	    join_type character varying(50) COLLATE pg_catalog."default",
	    record_count integer,
	    attributes character varying(1000) COLLATE pg_catalog."default",
	    file_id integer,
	    CONSTRAINT qsp_eng_flow_datalineage_pkey PRIMARY KEY (lineage_id)
	)';
	
	EXECUTE '
		CREATE TABLE '||schemaName||'.qsp_pipeline (
		pipeline_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
		pipeline_name varchar(100) NOT NULL,
		pipeline_type varchar(50) NOT NULL,
		active bool NULL DEFAULT true,
		created_by varchar(100) NULL,
		created_date timestamp NULL,
		modified_by varchar(100) NULL,
		modified_date timestamp NULL,
		is_published bool NULL DEFAULT false,
		CONSTRAINT qsp_pipeline_pk PRIMARY KEY (pipeline_id)
		)';

	EXECUTE '
		CREATE TABLE '||schemaName||'.qsp_dataset_schema (
		datasetschema_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
		folder_id int4 NOT NULL,
		pipeline_id int4 NOT NULL,
		object_name varchar(50) NULL,
		object_type varchar(50) NULL,
		sql_script text NULL,
		dataset_properties text NULL,
		created_by int2 NULL,
		created_date timestamp NULL,
		modified_by int2 NULL,
		modified_date timestamp NULL,
		schema_name varchar(100) NULL,
		sql_type bpchar(1) NULL,
		active bool NOT NULL DEFAULT false,
		CONSTRAINT qsp_dataset_schema_pk PRIMARY KEY (datasetschema_id)
		)';

		EXECUTE '
			CREATE TABLE '||schemaName||'.qsp_dataset_metadata (
			datasetmetadata_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
			datasetschema_id int4 NOT NULL,
			source_metadata text NULL,
			ui_metadata text NULL,
			target_metadata text NULL,
			"version" varchar(5) NULL,
			created_by int2 NULL,
			created_date timestamp NULL,
			modified_by int2 NULL,
			modified_date timestamp NULL,
			CONSTRAINT datasetmetadata_id_pk PRIMARY KEY (datasetmetadata_id)
			)';

			EXECUTE '
				CREATE TABLE '||schemaName||'.qsp_pipeline_connector_details (
				pc_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
				pipeline_id int4 NOT NULL,
				connector_id int4 NOT NULL
				)';

		EXECUTE '
			CREATE TABLE '||schemaName||'.qsp_pipeline_transcation_details (
			ptid int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
			pipeline_id int4 NOT NULL,
			pipeline_status int4 NOT NULL,
			pipeline_log text NULL,
			execution_date timestamp NOT NULL
			)';


		EXECUTE '
			CREATE TABLE  '||schemaName||'.qsp_connector_details (
			connector_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
			connector_name varchar(100) NOT NULL,
			connector_config text NOT NULL,
			connector_type varchar(20) NOT NULL,
			project_id int4 NOT NULL,
			active bool NULL DEFAULT true,
			created_by int4 NULL,
			created_date timestamp NULL,
			modified_by int4 NULL,
			modified_date timestamp NULL,
			CONSTRAINT qsp_connector_details_pk PRIMARY KEY (connector_id)
			)';

		EXECUTE '
			CREATE TABLE '||schemaName||'.qsp_folder_piidata (
			id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
			folder_id int4 NOT NULL,
			pii_data text NOT NULL,
			active bool NULL DEFAULT true,
			CONSTRAINT qsp_folder_piidata_pk PRIMARY KEY (id)
			)';

        EXECUTE '
            CREATE TABLE '||schemaName||'.qsp_redash_dashboard (
            rd_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
            rd_key varchar(255) NOT NULL,
            dashboard_name varchar(255) NULL,
            dashboard_id int4 NOT NULL,
            creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
            active bool NULL DEFAULT true,
            CONSTRAINT qsp_redash_dashboard_pkey PRIMARY KEY (rd_id)
            )';

        EXECUTE '
            CREATE TABLE '||schemaName||'.qsp_home_kpi (
            kpi_id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
            kpi_details varchar(255) NOT NULL,
            user_id int4 NOT NULL,
            CONSTRAINT qsp_home_kpi_pkey PRIMARY KEY (kpi_id)
            )';

	--- Table Creation completed..

	--- Trigger Creation started..

	EXECUTE '
	CREATE TRIGGER cleanse_job_changes
	    AFTER INSERT OR UPDATE
	    ON '||schemaName||'.qsp_run_job_status
	    FOR EACH ROW
    	    EXECUTE PROCEDURE public.log_cleanse_job_changes()';

	--- Trigger Creation completed..

	--- View Creation started...

	EXECUTE '
	CREATE OR REPLACE VIEW '||schemaName||'.qsp_folder_view AS
		SELECT folder.folder_name, FOLDER.FOLDER_DESC,
		    FOLDER.PROJECT_ID, PROJ.PROJECT_NAME,
		    FOLDER.USER_ID, CONCAT(USERPROFILE.USER_FIRST_NAME,'' '' ,USERPROFILE.USER_LAST_NAME) AS USERNAME,
		    FOLDER.CREATED_DATE, FOLDER.ACTIVE
		   FROM '||schemaName||'.QSP_FOLDER FOLDER
		   INNER JOIN PUBLIC.QS_PROJECT PROJ ON FOLDER.PROJECT_ID = PROJ.PROJECT_ID
		   INNER JOIN PUBLIC.QS_USER_V2 USERV2 ON FOLDER.USER_ID = USERV2.USER_ID
   		   INNER JOIN PUBLIC.QS_USERPROFILE USERPROFILE ON USERV2.USER_ID = USERPROFILE.USER_ID';

	EXECUTE '
	GRANT SELECT ON '||schemaName||'.qsp_folder_view TO postgres';

	EXECUTE '
	CREATE OR REPLACE VIEW '||schemaName||'.qsp_engflow_metadata_awsref_view AS
		SELECT athena_table, created_date, eng_flow_id, eng_flow_meta_data,
		flow_type, table_partition
		FROM '||schemaName||'.qsp_engflow_metadata_awsref';

	EXECUTE '
	GRANT SELECT ON '||schemaName||'.qsp_engflow_metadata_awsref_view TO postgres';

	EXECUTE '
	CREATE OR REPLACE VIEW '||schemaName||'.qsp_eng_flow_view as
		SELECT eng_flow_name, eng_flow_desc, eng_flow_meta_data,
		active, parent_eng_flow_id, project_id, eng_flow_config,
		created_date, created_by
		FROM '||schemaName||'.qsp_eng_flow';

	EXECUTE '
	GRANT SELECT ON '||schemaName||'.qsp_eng_flow_view TO postgres';

	EXECUTE format('INSERT INTO %I.qsp_redash_view_info(schema_name, view_name, active, creation_date)
	    values (%L,%L,true,now());', schemaName, schemaName, 'qsp_folder_view');

	EXECUTE format('INSERT INTO %I.qsp_redash_view_info(schema_name, view_name, active, creation_date)
	    values (%L,%L,true,now());', schemaName, schemaName, 'qsp_engflow_metadata_awsref_view');

	EXECUTE format('INSERT INTO %I.qsp_redash_view_info(schema_name, view_name, active, creation_date)
    	    values (%L,%L,true,now());', schemaName, schemaName, 'qsp_eng_flow_view');

	--- View Creation completed...

	result := 0;

	-- Completed on 2020-12-23 14:55:32

	--
	-- PostgreSQL database dump complete
	--

	EXCEPTION
	  WHEN SQLSTATE 'P0001' THEN
	    result := -1;

	end;
$BODY$;

ALTER PROCEDURE public.create_project_schema_and_tables(character varying, integer)
    OWNER TO postgres;



-- Procedure to create eng tables

-- PROCEDURE: public.create_eng_flow_job_result_table(character varying, character varying, character varying, character varying, character varying, character varying, integer)

-- DROP PROCEDURE IF EXISTS public.create_eng_flow_job_result_table(character varying, character varying, character varying, character varying, character varying, character varying, integer);

CREATE OR REPLACE PROCEDURE public.create_eng_flow_job_result_table(
	createtablequery character varying,
	schemaname character varying,
	tablename character varying,
	primarykeyname character varying,
	viewname character varying,
	viewcreatequery character varying,
	INOUT result integer)
LANGUAGE 'plpgsql'
AS $BODY$
BEGIN

	SET default_tablespace = '';
	SET default_table_access_method = heap;

	--
	-- TOC entry 504 (class 1259 OID 179111)
	-- Name: qsp_datadictionary; Type: TABLE; Schema: schemaName; Owner: postgres
	--

	EXECUTE createtablequery;

	EXECUTE '
	ALTER TABLE '||schemaname||'.'||tablename||' OWNER TO postgres';

	EXECUTE viewcreatequery;

	EXECUTE '
	GRANT SELECT ON '||schemaname||'.'||viewname||' TO postgres';

	result := 0;

	-- Completed on 2020-12-23 14:55:32

	--
	-- PostgreSQL database dump complete
	--

	EXCEPTION
	  WHEN SQLSTATE 'P0001' THEN
	    result := -1;

	end;
$BODY$;

ALTER PROCEDURE public.create_eng_flow_job_result_table(character varying, character varying, character varying, character varying, character varying, character varying, integer)
    OWNER TO postgres;




-- Procedure to insert eng flow details

-- PROCEDURE: public.insert_eng_flow_job_result_rows(character varying, integer)

-- DROP PROCEDURE IF EXISTS public.insert_eng_flow_job_result_rows(character varying, integer);

CREATE OR REPLACE PROCEDURE public.insert_eng_flow_job_result_rows(
	insertquery character varying,
	INOUT result integer)
LANGUAGE 'plpgsql'
AS $BODY$
	BEGIN

	SET default_tablespace = '';
	SET default_table_access_method = heap;

	--
	-- TOC entry 504 (class 1259 OID 179111)
	-- Name: qsp_datadictionary; Type: TABLE; Schema: schemaName; Owner: postgres
	--

	EXECUTE insertquery;

	result := 0;

	-- Completed on 2020-12-23 14:55:32

	--
	-- PostgreSQL database dump complete
	--

	EXCEPTION
	  WHEN SQLSTATE 'P0001' THEN
	    result := -1;

	end;

$BODY$;

ALTER PROCEDURE public.insert_eng_flow_job_result_rows(character varying, integer)
    OWNER TO postgres;


-- Add master data



INSERT INTO public.qs_datasource_types (data_source_type,data_source_name,data_source_image,active,creation_date) VALUES
	 ('LocalFile','Local File','folder.png',true,'2021-01-30 13:58:07.772487'),
	 ('api','API','api.png',true,'2021-02-13 12:11:09'),
	 ('DB','PgSQL','pgsql.png',true,'2021-02-13 12:11:09');


INSERT INTO public.qs_product_features ("module",feature,active,subscription_type,creation_date,modified_date) VALUES
	 ('Ingestion','API',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Ingestion','Delta loads in same folder',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','Column level stats',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','Frequency analysis',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','Duplicates',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','Nulls',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','Min, Max, Mean, Median',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','Data quality rules monitoring for valid and invalid data',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Cleanse','Invalid data record view',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Cleanse','Up to 50 different rules',true,'Trial','2021-03-28 04:26:28.164625',NULL);
INSERT INTO public.qs_product_features ("module",feature,active,subscription_type,creation_date,modified_date) VALUES
	 ('Cleanse','Add unlimited rules on a dataset',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Cleanse','Rules audit',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Cleanse','Automatic rules cloning',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Engineer','Write now, run later option',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Engineer','Browse from raw, cleansed, engineered file',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Engineer','Joins infinite files',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Engineer','Aggregate function with sum, mean, median',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Automate','Option to save as flat file or Relational DB',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Automate','Run single cleansed file',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Automate','Run engineered dataset',true,'Trial','2021-03-28 04:26:28.164625',NULL);
INSERT INTO public.qs_product_features ("module",feature,active,subscription_type,creation_date,modified_date) VALUES
	 ('Automate','Run jobs on different versioned files',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Automate','Re-run job',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Automate','View history',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Govern','Download processed data',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Govern','Create attribute linked business glossary catalogue',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Govern','Implement data governance by assigning Data ownership and stewardship',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Govern','Manage business glossary and data dictionary in a single place',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Dashboard','Data quality rules limited to the data attributes',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Dashboard','Retrieve data from raw, cleansed and engineered data',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Dashboard','Create beautiful dashboard and charts',true,'Trial','2021-03-28 04:26:28.164625',NULL);
INSERT INTO public.qs_product_features ("module",feature,active,subscription_type,creation_date,modified_date) VALUES
	 ('Dashboard','Create alerts',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Dashboard','Share dashboards internally and globally',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Ingestion','PostgreSQL',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Profile','View data in split seconds',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Engineer','Live preview of joined data',true,'Trial','2021-03-28 04:26:28.164625',NULL),
	 ('Govern','Create data dictionary with data quality rules',true,'Trial','2021-03-28 04:26:28.164625',NULL);


	INSERT INTO public.qs_redash_view_info (schema_name,view_name,active,creation_date) VALUES
	 ('qs_automatic','qsp_engflow_metadata_awsref_view',true,'2021-01-30 10:25:01.531808'),
	 ('qs_automatic','qsp_folder_view',true,'2021-01-30 10:25:01.531808'),
	 ('qs_automatic','qsp_eng_flow_view',true,'2021-01-30 10:25:01.531808');

	INSERT INTO public.qs_redash_view_info (schema_name,view_name,active,creation_date) VALUES
	 ('qs_automatic','qsp_engflow_metadata_awsref_view',true,'2021-01-30 10:25:01.531808'),
	 ('qs_automatic','qsp_folder_view',true,'2021-01-30 10:25:01.531808'),
	 ('qs_automatic','qsp_eng_flow_view',true,'2021-01-30 10:25:01.531808');


	INSERT INTO public.qs_subscription_v2 (subscription_id,"name",plan_type,plan_type_price,accounts,plan_type_account_price,validity_days,plan_type_id,plan_type_currency,plan_type_settings,active) VALUES
(2,'Starter','Monthly',120.0,1,14.0,730,'price_1K4xCICXjfdxZzBVAElzMgky','USD','
{
"cumulative_size_bytes":"10000000000",
"max_file_size":"10000000"
}
',true), (1,'Trial','Monthly',0.0,1,5.0,730,'price_1IlIB1CXjfdxZzBVsDPYAgZv','USD','{
"cumulative_size_bytes":"50000000",
"max_file_size":"500000"
}',true);
	
	
	INSERT INTO public.qs_rules_catalog (created_by,created_date,modified_by,modified_date,rule_contents,rule_name,user_id) VALUES
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def missingWithNullValuesParam(value):
    if not value:
        # csv file consider the null as empty. send the null as string
        return ''null''
    
    return value

missingWithNullValuesUDF = udf(missingWithNullValuesParam, StringType())

def missingWithNullValues(columns):
    
    dfNew = df
    
    if not columns:
        return dfNew

    columns = [x.strip() for x in columns.split('','')]
    
    for column in columns:
        dfNew = dfNew.withColumn(column, missingWithNullValuesUDF(col(column)))

    return dfNew','fillNullWithNullValue',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def contains(x, valueToCompare, keepMatching):
    try:
        if keepMatching == "Keep":
            return df.filter(col(x).contains(valueToCompare))
        else:
            return df.filter(~(col(x).contains(valueToCompare)))
    except Py4JJavaError as e:
        return df','filterRowsByColumnContains',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def endsWith(x, valueToCompare, keepMatching):
    try:
        if keepMatching == "Keep":
            return df.filter(col(x).like(''%{0}''.format(valueToCompare)))
        else:
            return df.filter(~(col(x).like(''%{0}''.format(valueToCompare))))
    except Py4JJavaError as e:
        return df','filterRowsByColumnEndsWith',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def countByDelimiterParam(value, startDelimiter, endDelimiter):
    if not value:
        return value
    result = re.findall(''{}(.*?){}''.format(startDelimiter, endDelimiter), str(value))
    return len(result)

count_by_delimiter_udf = udf(countByDelimiterParam, StringType())

def countByDelimiter(column, value1, value2, new_column):
    dfNew = df.withColumn(new_column, count_by_delimiter_udf(col(column), lit(value1), lit(value2)))
    return dfNew','countMatchDelimiter',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def countByCharacterParam(value, params):
    if not value:
        return value
    value = str(value)
    return value.count(params)

count_by_character_udf = udf(countByCharacterParam, StringType())

def countByCharacter(column, values, new_column):
    dfNew = df.withColumn(new_column, count_by_character_udf(col(column), lit(values)))
    return dfNew','countMatchText',1),
	 ('admin','2021-02-13 17:59:47.548',NULL,NULL,'def extractBetweenTwoDelimitersParams(value, startDelimiter, endDelimiter, currentCount=1):
    result = re.findall(''{}(.*?){}''.format(startDelimiter, endDelimiter), str(value))
    
    if startDelimiter and not endDelimiter:
        # only start delimiter not end delimeter.
        startIndex = value.find(startDelimiter)
        return value[startIndex + 1 :len(value)]
    elif endDelimiter and not startDelimiter:
        # only end delimiter not start delimiter.
        endIndex = value.find(endDelimiter)
        return value[0:endIndex]
    elif len(result) >= currentCount:
        # Both delimiter are present.
        return result[currentCount - 1]
extractBetweenTwoDelimitersParamsUDF = udf(extractBetweenTwoDelimitersParams, StringType())
def extractBetweenTwoDelimiters(x, startDelimiter, endDelimiter, numberOfTimesToExtract, columns):
    df1 = df
    columns = [x.strip() for x in columns.split('','')]
    
    if numberOfTimesToExtract > 1 and startDelimiter and endDelimiter:
        for count in range(numberOfTimesToExtract):
            df1 = df1.withColumn(columns[count], extractBetweenTwoDelimitersParamsUDF(col(x), lit(startDelimiter), lit(endDelimiter), lit(count + 1)))
    else:
        df1 = df1.withColumn(columns[0], extractBetweenTwoDelimitersParamsUDF(col(x), lit(startDelimiter), lit(endDelimiter)))
    return df1','extractColumnValuesBetweenTwoDelimiters',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def typeMismatched(x, dataType, columns):
    columns = [x.strip() for x in columns.split('','')]
    return df.withColumn(columns[0], df[x].cast(dataType))','extractColumnValuesTypeMismatched',1),
	 ('admin','2021-02-13 17:59:47.548',NULL,NULL,'def extractNumberParams(value, currentCount=1):
    value = str(value)
    data = re.findall(r''\d+'', value)
    if data and len(data) > currentCount:
        return data[currentCount]
    else:
        return ''''
extractNumberParamsUDF = udf(extractNumberParams, StringType())
def extractNumber(x, numberOfTimesToExtract, columns):
    dfNew = df
    
    columns = [x.strip() for x in columns.split('','')]
    for count in range(numberOfTimesToExtract):
        dfNew = dfNew.withColumn(columns[count], extractNumberParamsUDF(col(x), lit(count)))
        
    return dfNew','extractColumnValuesNumbers',1),
	 ('admin','2021-02-27 06:39:44.428',NULL,NULL,'def standardize(x, valuesToReplace, newValue):    
    if not x:
        return df
    
    valuesToReplace = [x for x in valuesToReplace.split(''|'')]
    
    return df.withColumn(x , when(col(x).isin(valuesToReplace) , newValue).otherwise(col(x)))','standardize',1),
	 ('admin','2021-02-13 17:59:47.548',NULL,NULL,'def extractTextOrPatternParams(value, txtToExtract, startExtract, endExtract, currentCount=1):
    if currentCount == 1:
        result = re.findall(''{}(.*?){}''.format(startExtract, endExtract), str(value))
        if any(txtToExtract in s for s in result):
            return txtToExtract
    else:
        result = re.findall(''{}(.*?){}''.format(startExtract, endExtract), str(value))
        if len(result) >= currentCount:
            return txtToExtract

extractTextOrPatternParamsUDF = udf(extractTextOrPatternParams, StringType())

def extractTextOrPattern(x, txtToExtract, startExtract, endExtract, numberOfTimesToExtract=1):
    df1 = df
    count = 0
    if numberOfTimesToExtract > 1:
        for item in range(numberOfTimesToExtract):
            count += 1
            df1 = df1.withColumn(''{}{}''.format(x, count), extractTextOrPatternParamsUDF(col(x), lit(txtToExtract), lit(startExtract), lit(endExtract), lit(count)))
    else:
        df1 = df1.withColumn(''{}1''.format(x), extractTextOrPatternParamsUDF(col(x), lit(txtToExtract), lit(startExtract), lit(endExtract)))
    return df1','extractColumnValuesTextOrPattern',1);
INSERT INTO public.qs_rules_catalog (created_by,created_date,modified_by,modified_date,rule_contents,rule_name,user_id) VALUES
	 ('admin','2021-02-13 11:03:34.028',NULL,NULL,'def fillNull(x,y):
   try:
     return df.fillna({x:y})
   except Py4JJavaError as e:
      return df
','fillNull',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def missingWithCustomValuesParam(value, customValues):
    if not value:
        return customValues
    return value

missing_with_custom_values_udf = udf(missingWithCustomValuesParam, StringType())

def missingWithCustomValues(columns, customValues):
    dfNew = df
    if not columns:
        return dfNew
    columns = [x.strip() for x in columns.split('','')]

    for column in columns:
        dfNew = dfNew.withColumn(column, missing_with_custom_values_udf(col(column), lit(customValues)))
    return dfNew','fillNullWithCustomValue',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def replaceCells(columns, oldValues, newValue):

    dfNew = df
    if not columns:
        return dfNew
    columns = [x.strip() for x in columns.split('','')]
    oldValues = [x.strip() for x in oldValues.split('','')]

    for column in columns:
        for val in oldValues: 
            dfNew = dfNew.withColumn(column, regexp_replace(col(column), val, newValue))

    return dfNew','findReplace',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def extractFirstNCharacters(x, noOfCharacters, columns):
    columns = [x.strip() for x in columns.split('','')]
    dfNew = df.withColumn(columns[0], col(x).substr(0, noOfCharacters))
    return dfNew','extractColumnValuesFirstNChars',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def extractLastNCharacters(x, noOfCharacters, columns):
    noOfCharacters -= 1
    columns = [x.strip() for x in columns.split('','')]
    dfNew = df.withColumn(columns[0], col(x).substr(F.length(x) - noOfCharacters, F.length(x)))
    
    return dfNew','extractColumnValuesLastNChars',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def topRowsAtRegularInterval(sortColumn, startFrom, interval, keepMatching):
    
    if not sortColumn:
        return df
    sortedDf = df.orderBy(sortColumn , ascending=True)
    temp_df = []
    count = 0    
    data = sortedDf.collect()
    
    if keepMatching == ''Keep'':
        del data[0: startFrom-1]
    else:
        temp_df = data[0: startFrom-1]
        del data[0: startFrom-1]
    for row in data:
        dict_row = row.asDict()
        if keepMatching == ''Keep'':    
            if count % interval == 0:
                temp_df.append(dict_row)
        else:
            if count % interval != 0:
                temp_df.append(dict_row)
        count += 1
    
    return spark.createDataFrame(temp_df, schema=sortedDf.schema)','filterRowsTopRowsAtRegularIntervalByColumn',1),
	 ('admin','2021-02-13 17:59:47.548',NULL,NULL,'def extractQueryparam(url, param):
    d = parse.parse_qs(parse.urlsplit(str(url)).query)
    if d.get(param):
        return d.get(param)[0]
extract_param_udf = udf(extractQueryparam, StringType())
def extractQueryString(x, params, columns):
    if not params:
        return df
    
    columns = [x.strip() for x in columns.split('','')]
    splitedItems = [x.strip() for x in params.split('','')]
    
    df1 = df
    count = 0
    for item in splitedItems:
        df1 = df1.withColumn(columns[count], extract_param_udf(col(x), lit(item)))
        count += 1
    
    return df1','extractColumnValuesQueryString',1),
	 ('admin','2021-02-13 11:03:34.028',NULL,NULL,'def removeSpecialCharacters(x):
    return df.withColumn(x, regexp_replace(x, r''[^a-zA-Z0-9 ]+'', ''''))','formatRemoveSpecialCharacters',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def charactersBetweenTwoPositions(x, startPosition, endPosition, columns):
    
    if endPosition <= 1:
        endPosition += 1
    else:
        endPosition -= startPosition
        
    startPosition += 1
    columns = [x.strip() for x in columns.split('','')]
    dfNew = df.withColumn(columns[0], col(x).substr(startPosition, endPosition))
    
    return dfNew','extractColumnValuesCharsInBetween',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def missingWithAverageValuesParam(value, avg_value):
    if not value:
        return avg_value
    return value
missingWithAverageValuesUDF = udf(missingWithAverageValuesParam, StringType())
def missingWithAverageValues(columns):
    
    dfNew = df
    if not columns:
        return dfNew
    columns = [x.strip() for x in columns.split('','')]
    
    for item in columns:
        try:
            avg_values = dfNew.select(F.avg(dfNew[item])).collect()
            avg_value = avg_values[0][0]
    
            dfNew = dfNew.withColumn(item, missingWithAverageValuesUDF(col(item), lit(str(avg_value))))
        except:
            dfNew = dfNew.na.fill(0, subset=[item])
    
    return dfNew','fillNullWithAverageValue',1);
INSERT INTO public.qs_rules_catalog (created_by,created_date,modified_by,modified_date,rule_contents,rule_name,user_id) VALUES
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def removeAscentsParams(value):    
    if not value:
        return value
    result = ''''.join([i if ord(i) < 128 else '''' for i in value])
    return result
   
removeAscentsUDF = udf(removeAscentsParams, StringType())
def removeAscents(x):
    return df.withColumn(x, removeAscentsUDF(col(x)))','formatRemoveAscents',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def missingWithSumValuesParam(value, sum_value):
    if not value:
        return sum_value
    return value

missingWithSumValuesUDF = udf(missingWithSumValuesParam, StringType())

def missingWithSumValues(columns):
    dfNew = df
    if not columns:
        return dfNew
    columns = [x.strip() for x in columns.split('','')]
    for item in columns:
        try:
            sum_values = dfNew.select(F.sum(dfNew[item])).collect()
            sum_value = sum_values[0][0]

            dfNew = dfNew.withColumn(item, missingWithSumValuesUDF(col(item), lit(sum_value)))
        except:
            dfNew = dfNew.na.fill("0", [item])
    return dfNew','fillNullWithSumValue',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def addSuffix(x,y):
   try:
      return df.withColumn(x, concat(col(x), lit(y)))
   except Py4JJavaError as e:
      return df','formatAddSuffix',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def formatLower(columns):
    dfNew = df
    if not columns:
        return df

    columns = [x.strip() for x in columns.split('','')]
    for column in columns:
        dfNew = dfNew.withColumn(column, lower(col(column)))
    return dfNew','formatLower',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def padWithLeadingCharacters(x,n,y):
   try:
      return df.withColumn(x, lpad(x, n , y))
   except Py4JJavaError as e:
      return df','formatPadWithLeadingCharacters',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def isExactly(x, valueToCompare, keepMatching):
    try:
        if keepMatching == "Keep":
            return df.filter(df[x] == valueToCompare)
        else:
            return df.filter(~(df[x] == valueToCompare))
    except Py4JJavaError as e:
        return df','filterRowsByColumnExactly',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def notEqualTo(x, valueToCompare, keepMatching):
    try:
        if keepMatching == "Keep":
            return df.filter(df[x] != valueToCompare)
        else:
            return df.filter(df[x] == valueToCompare)
    except Py4JJavaError as e:
        return df','filterRowsByColumnNotEqualTo',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def startsWith(x, valueToCompare, keepMatching):
    try:
        if keepMatching == "Keep":
            return df.filter(col(x).like(''{0}%''.format(valueToCompare)))
        else:
            return df.filter(~(col(x).like(''{0}%''.format(valueToCompare))))
    except Py4JJavaError as e:
        return df','filterRowsByColumnStartsWith',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def columnValueIsOneOf(colName, valueToCompare, keepMatching):
    try:
        if not valueToCompare:
            return df
        values = [x.strip() for x in valueToCompare.split('','')]
        if keepMatching == "Keep":
            return df.filter(df[colName].isin(values))
        else:
            return df.filter(~df[colName].isin(values))
    except Py4JJavaError as e:
        return df','filterRowsByColumnValueIsOneOf',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def rangeRows(start, end, keepMatching):
    try:
        if keepMatching == "Keep":
            newDf = df.collect()[start - 1 : end]
            return spark.createDataFrame(newDf) 
        else:
            df1 = df.collect()[0 : start - 1]
            df2 = df.collect()[end : ]
            return spark.createDataFrame(df1 + df2)
    except Py4JJavaError as e:
        return df','filterRowsRangeRows',1);
INSERT INTO public.qs_rules_catalog (created_by,created_date,modified_by,modified_date,rule_contents,rule_name,user_id) VALUES
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def rangeRowsByColumn(start, end, sortedColumns='', keepMatching):
    try:
        dfNew = df
        if sortedColumns:
            columns = [x.strip() for x in sortedColumns.split('','')]
            dfNew = df.orderBy(columns , ascending=[0,0])
        if keepMatching == "Keep":
            df1 = dfNew.collect()[start - 1 : end]
            return spark.createDataFrame(df1)
        else:
            df1 = dfNew.collect()[0 : start - 1]
            df2 = dfNew.collect()[end : ]
            return spark.createDataFrame(df1 + df2)
    except Py4JJavaError as e:
        return df','filterRowsRangeRowsByColumn',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def createColumn(column, column_new, data):
    return df.withColumn(column_new, lit(data))','manageColumnsCreate',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def dropColumn(x):  
    try: 
        return df.drop(x) 
    except Py4JJavaError as e: 
        return df','manageColumnsDrop',1),
	 ('admin','2021-03-02 10:32:13.703',NULL,NULL,'def renameColumn(column, new_column_name):

    dfNew = df.withColumn(new_column_name, df[column])

    position = dfNew.columns.index(column)
    new_column_position = dfNew.columns.index(new_column_name)

    columns = dfNew.columns
    columns.pop(new_column_position)
    columns.pop(position)
    columns.insert(position, new_column_name)

    return dfNew.select(((*columns)))','manageColumnsRename',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def mergeRule(columns, new_column_name, seperator="-"):

    if not columns:
        return df

    columns = [x.strip() for x in columns.split('','')]

    count = 1
    dfNew = df
    for column in columns:
        if count == 1:
            dfNew = dfNew.withColumn(new_column_name, concat(col(column), lit(seperator)))
        else:
            if count == len(columns):
                dfNew= dfNew.withColumn(new_column_name, concat(col(new_column_name), col(column)))
            else:
                dfNew= dfNew.withColumn(new_column_name, concat(col(new_column_name), col(column), lit(seperator)))



        count += 1
    return dfNew','mergeRule',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def replaceTextOrPattern(columns, oldValue, newValue):
    dfNew = df
    if not columns:
        return dfNew

    columns = [x.strip() for x in columns.split('','')]

    for item in columns:
        dfNew = dfNew.withColumn(item, regexp_replace(col(item), oldValue, newValue))
    return dfNew','patternReplace',1),
	 ('admin','2021-02-13 11:03:34.028',NULL,NULL,'def removeDuplicateRows():  
    try: 
        return df.dropDuplicates() 
    except Py4JJavaError as e: 
        return df','removeDuplicateRows',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def addPrefix(x,y):
   try:
      return df.withColumn(x, concat(lit(y), col(x)))
   except Py4JJavaError as e:
      return df','formatAddPrefix',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def topRows(noOfRecords, keepMatching):
    if keepMatching == ''Keep'':
        return df.limit(noOfRecords)
    tmpData = df.collect()[noOfRecords:-1]
    return spark.createDataFrame(tmpData, schema=df.schema)','filterRowsTopRows',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def missingRuleParams(value, keepmatching):
    if keepmatching and not value:
        return True
    elif value:
        return False
missingUDF = udf(missingRuleParams, BooleanType())

def isMissing(x, keepMatching):
    if keepMatching == ''Keep'':
        return df.filter(missingUDF(col(x), lit(True)))
    else:
        return df.filter(~missingUDF(col(x), lit(False)))','filterRowsByColumnMissing',1);
INSERT INTO public.qs_rules_catalog (created_by,created_date,modified_by,modified_date,rule_contents,rule_name,user_id) VALUES
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def splitByMultipleDelimiterParams(s, delimiters):
    s = str(s)
    delimiters = [x.strip() for x in delimiters.split('','')]
    splitedData = []

    count = 0
    for d in delimiters:
        index = s.find(d)
        splitedStr = s[0: index]
        splitedData.append(splitedStr)
        s = s[len(splitedStr) + 1 : ]
    splitedData.append(s)

    return splitedData

splitByMultipleDelimiterUDF = udf(splitByMultipleDelimiterParams, ArrayType(StringType()))

def splitByMultipleDelimiter(x, delimiters):
    dfNew = df
    splited = delimiters.split('','')
    count = 0
    for d in splited:
        dfNew = dfNew.withColumn("{}{}".format(x ,count+1), splitByMultipleDelimiterUDF(col(x), lit(delimiters)).getItem(count))
        count += 1
    dfNew = dfNew.withColumn("{}{}".format(x ,count+1), splitByMultipleDelimiterUDF(col(x), lit(delimiters)).getItem(count))
    dfNew = dfNew.drop(x)
    return dfNew','splitByMultipleDelimiters',1),
	 ('admin','2021-02-14 13:23:59.524',NULL,NULL,'def splitByRegularIntervalParams(s, startPosition, interval, numberOfTimesToSplit):
    splitedData = []
    splitedData.append(s[0:1])
    count = 1
    s = s[1: ]

    for item in range(numberOfTimesToSplit - 1):
        splitedStr = s[0 : interval] 
        splitedData.append(splitedStr)
        s = s[len(splitedStr) : ]
    splitedData.append(s)
    return splitedData

splitByRegularIntervalUDF = udf(splitByRegularIntervalParams, ArrayType(StringType()))

def splitByRegularInterval(x, startPosition, interval, noOfTimesToSplit):
    dfNew = df
    count = 0
    for item in range(noOfTimesToSplit + 1):
        dfNew = dfNew.withColumn("{}{}".format(x, count+1), 
                             splitByRegularIntervalUDF(col(x), 
                                                       lit(startPosition), lit(interval), lit(noOfTimesToSplit)).getItem(count))
        count += 1
    dfNew = dfNew.drop(x)
    return dfNew','splitByRegularInterval',1),
	 ('admin','2021-02-13 11:03:34.028',NULL,NULL,'def trimQuotes(x):
    try:
        return df.withColumn(x, regexp_replace(x, ''"'', ''''))
    except Py4JJavaError as e:
        return df','formatTrimQuotes',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def formatUpper(columns):
    dfNew = df
    if not columns:
        return df

    columns = [x.strip() for x in columns.split('','')]
    for column in columns:
        dfNew = dfNew.withColumn(column, upper(col(column)))
    return dfNew','formatUpper',1),
	 ('admin','2021-03-02 10:32:13.703',NULL,NULL,'def cloneColumn(column):
    dfNew = df.withColumn(''{}_clone1''.format(column), df[column])
    return dfNew','manageColumnsClone',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def splitParam(s, delimiter):
    s = str(s)
    splitedExpression = r''{}''.format(delimiter)
    return s.split(splitedExpression, 1)
splitFirst = udf(splitParam, ArrayType(StringType()))

def splitByDelimiter(x, splitedExpression, newColumnNames):
    columns = [x.strip() for x in newColumnNames.split('','')]
    
    dfNew = df
    df2 = dfNew.withColumn("tmpCol", splitFirst(col(x), lit(splitedExpression)))
    df_sizes = df2.select(F.size(''tmpCol'').alias(''tmpCol''))
    df_max = df_sizes.agg(F.max(''tmpCol''))
    nb_columns = df_max.collect()[0][0]
    if nb_columns == 1:
        df2 = df2.withColumn(columns[0], lit(""))
        df2 = df2.withColumn(columns[1], col(x))
        df2 = df2.drop(''tmpCol'')
    else:
        df2 = df2.select(*df2.columns, *[(df2[''tmpCol''][i]).alias(columns[i]) for i in range(nb_columns)]).drop(''tmpCol'')
    return df2','splitByDelimiter',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def splitBetweenTwoDelimiterParams(s, delimiter1, delimiter2):
    s = str(s)
    firstIndex = s.find(delimiter1)
    secondIndex = s.find(delimiter2, firstIndex + 1)
    
    if firstIndex == -1 or secondIndex == -1:
        return [s, '''']
    result = [s[0 : firstIndex], s[secondIndex + 1: len(s)]]
    return result 
splitUDF = udf(splitBetweenTwoDelimiterParams, ArrayType(StringType()))

def splitBetweenTwoDelimiters(x, delimiter1, delimiter2, columns):
    
    columns = [x.strip() for x in columns.split('','')]
    
    dfNew = df
    dfNew = dfNew.withColumn(columns[0], splitUDF(col(x), lit(delimiter1), lit(delimiter2)).getItem(0))
    dfNew = dfNew.withColumn(columns[1], splitUDF(col(x), lit(delimiter1), lit(delimiter2)).getItem(1))
    
    return dfNew','splitBetweenTwoDelimiters',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def splitBetweenTwoPositions(x, position1, position2, columns):
    
    columns = [x.strip() for x in columns.split('','')]
    dfNew = df
    dfNew = dfNew.withColumn(columns[0], col(x).substr(0, position1))
    dfNew = dfNew.withColumn(columns[1], col(x).substr(lit(position2 + 1), F.length(x)))
    
    return dfNew','splitBetweenTwoPositions',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def splitAtPositions(x, positions, columns):
    
    if not positions:
        return df
    positions = [x.strip() for x in positions.split('','')]
    columns = [x.strip() for x in columns.split('','')]
    fp = int(positions[0])
    sp = fp
    count = 0
    dfNew = df.withColumn(columns[count], col(x).substr(lit(0), lit(fp)))
    for item in positions:
        if count  < len(positions) - 1:
            fp = int(positions[count])
            sp = int(positions[count + 1])
            count += 1
            dfNew = dfNew.withColumn(columns[count], col(x).substr(lit(fp + 1), lit(sp-fp)))
 
    dfNew = dfNew.withColumn(columns[count + 1], col(x).substr(lit(sp + 1), F.length(x)))
    
    return dfNew','splitAtPositions',1),
	 ('admin','2021-04-26 17:04:53.196107',NULL,NULL,'def trimWhitespaces(x):
   try:
      return df.withColumn(x, trim(col(x)))
   except Py4JJavaError as e:
      return df','formatTrimWhitespace',1);
INSERT INTO public.qs_rules_catalog (created_by,created_date,modified_by,modified_date,rule_contents,rule_name,user_id) VALUES
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def removeTrimParams(value):
    if not value:
        return value

    value = str(value)
    return value.replace(" ","")

removeTrimUDF = udf(removeTrimParams, StringType())

def removeTrim(x):
    return df.withColumn(x, removeTrimUDF(col(x)))','formatRemoveWhitespaces',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def formatToProperCaseParams(value):
    if not value:
        return value
    return value.title()

formatToProperCaseUDF = udf(formatToProperCaseParams, StringType())

def formatProper(columns):
    dfNew = df
    if not columns:
        return df
    columns = [x.strip() for x in columns.split('','')]
    for column in columns:
        dfNew = dfNew.withColumn(column, formatToProperCaseUDF(col(column)))
    return dfNew','formatProper',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def greaterThanOrEqualToParams(value, valueToCompare, keepMatching):
    try:
        value = float(value)
        if keepMatching and value >= valueToCompare:
            return ''KEEP''
        elif not value > valueToCompare and value != valueToCompare:
            return ''REMOVE''
    except:
        if not keepMatching:
            return ''VALID''
greaterThanOrEqualToUDF = udf(greaterThanOrEqualToParams, StringType())
def greaterThanOrEqualTo(x, valueToCompare, keepMatching=True):
    dfNew = df
    
    valueToCompare = float(valueToCompare)
    if keepMatching == ''Keep'':
        dfNew = df.withColumn(''tmp'', greaterThanOrEqualToUDF(col(x), lit(valueToCompare), lit(True)))
        dfNew = dfNew.filter((f.col(''tmp'') == ''KEEP'') | (f.col(''tmp'') == ''VALID''))
    else:
        dfNew = df.withColumn(''tmp'', greaterThanOrEqualToUDF(col(x), lit(valueToCompare), lit(False)))
        dfNew = dfNew.filter((f.col(''tmp'') == ''REMOVE'') | (f.col(''tmp'') == ''VALID''))
    
    dfNew = dfNew.drop(''tmp'')
    
    return dfNew','filterRowsByColumnGreaterThanOrEqualTo',1),
	 ('admin','2021-02-27 05:27:31.672',NULL,NULL,'def missingWithLatestValuesParams(value, LatestValues):
    value = str(value)
    if not value.strip() or value == ''None'':
        return LatestValues
    
    return value
missingWithLatestValuesParamsUDF = udf(missingWithLatestValuesParams, StringType())

def missingWithLatestValues(columns):
    
    dfNew = df
    if not columns:
        return dfNew
    columns = [x.strip() for x in columns.split('','')]
    
    for item in columns:
        try:
            last_value = ''''
            reverse_df = df.collect()
            reverse_df.reverse()
            for row in reverse_df:
                
                if isinstance(row[item], str):
                    last_value = row[item].strip()
                else:
                    last_value = row[item]
                
                if last_value:
                    break
            dfNew = dfNew.withColumn(item, missingWithLatestValuesParamsUDF(col(item), lit(last_value)))
        except:
            dfNew = dfNew.na.fill(0, subset=[item])
    
    return dfNew','fillNullWithLastValue',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def topRowsByColumn(noOfRecords, sortedColumn, keepMatching=True):
    try:
        if not sortedColumn:
            return df

        columns = [x.strip() for x in sortedColumn.split('','')]
        sortedDf = df.orderBy(columns , ascending=[1,1])
        if keepMatching == "Keep":
            return sortedDf.limit(noOfRecords)
        else:
            tmpData = sortedDf.collect()[noOfRecords:-1]
            return spark.createDataFrame(tmpData, schema=df.schema)
    except Py4JJavaError as e:
        return df','filterRowsTopRowsByColumn',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def isBetweenParams(value, startValue, endValue, keepMatching):
    try:
        value = float(value)
        if keepMatching and value >= startValue and value <= endValue:
            return ''KEEP''
        elif not (value >= startValue and value <= endValue):
            return ''REMOVE''
    except:
        if not keepMatching:
            return ''VALID''
isBetweenParamsUDF = udf(isBetweenParams, StringType())
def isBetween(x, startValue, endValue, keepMatching):
    
    dfNew = df
    
    startValue = float(startValue)
    endValue = float(endValue)
    if keepMatching == ''Keep'':
        dfNew = df.withColumn(''tmp'', isBetweenParamsUDF(col(x), lit(startValue), lit(endValue), lit(True)))
        dfNew = dfNew.filter((f.col(''tmp'') == ''KEEP'') | (f.col(''tmp'') == ''VALID''))
    else:
        dfNew = df.withColumn(''tmp'', isBetweenParamsUDF(col(x), lit(startValue), lit(endValue), lit(False)))
        dfNew = dfNew.filter((f.col(''tmp'') == ''REMOVE'') | (f.col(''tmp'') == ''VALID''))
        
    dfNew = dfNew.drop(''tmp'')
    
    return dfNew','filterRowsByColumnBetween',1),
	 ('admin','2021-02-13 10:31:47.224',NULL,NULL,'def lessThanOrEqualToParams(value, valueToCompare, keepMatching):
    try:
        value = float(value)
        if keepMatching and value <= valueToCompare:
            return ''KEEP''
        elif not value < valueToCompare and value != valueToCompare:
            return ''REMOVE''
    except:
        if not keepMatching:
            return ''VALID''
lessThanOrEqualToUDF = udf(lessThanOrEqualToParams, StringType())
def lessThanOrEqualTo(x, valueToCompare, keepMatching):
    dfNew = df
    
    valueToCompare = float(valueToCompare)
    if keepMatching == ''Keep'':
        dfNew = df.withColumn(''tmp'', lessThanOrEqualToUDF(col(x), lit(valueToCompare), lit(True)))
        dfNew = dfNew.filter((f.col(''tmp'') == ''KEEP'') | (f.col(''tmp'') == ''VALID''))
    else:
        dfNew = df.withColumn(''tmp'', lessThanOrEqualToUDF(col(x), lit(valueToCompare), lit(False)))
        dfNew = dfNew.filter((f.col(''tmp'') == ''REMOVE'') | (f.col(''tmp'') == ''VALID''))
    
    dfNew = dfNew.drop(''tmp'')
    
    return dfNew','filterRowsByColumnLessThanOrEqualTo',1),
	 ('admin','2021-06-08 17:04:53','','2021-06-08 17:04:53','def topRowsAtRegularIntervalByDefault(startFrom, interval, keepMatching):
    dfNew = df    
    temp_df = []
    count = 0
    data = dfNew.collect()
    if keepMatching == ''Keep'':
        del data[0: startFrom-1]
    else:
        temp_df = data[0: startFrom-1]
        del data[0: startFrom-1]
    for row in data:
        dict_row = row.asDict()
        if keepMatching == ''Keep'':    
            if count % interval == 0:
                temp_df.append(dict_row)
        else:
            if count % interval != 0:
                temp_df.append(dict_row)
        count += 1
    
    return spark.createDataFrame(temp_df, schema=dfNew.schema)','filterRowsTopRowsAtRegularInterval',1),
	 ('admin','2021-02-28 11:14:59.31',NULL,NULL,'def missingWithModeValuesParam(value, mode_value):
    if not value:
        return mode_value
    return value
missingWithModeValuesUDF = udf(missingWithModeValuesParam, StringType())
def missingWithModValues(columns):
    
    dfNew = df
    if not columns:
        return dfNew
    columns = [x.strip() for x in columns.split('','')]
    
    for item in columns:
        try:
            df_temp = dfNew.groupBy(item).count()
            df_temp = df_temp.sort(col(item),col(''count''))
            data = df_temp.collect()
            
            isFound = 0
            count = 0
            mode_value = 0
            
            
            for row in data:
                
                if row[item] and not isFound:
                    count = row[''count'']
                    mode_value = row[item]
                    isFound = True
                if row[''count''] > count:
                    count = row[''count'']
                    mode_value = row[item]
            
            dfNew = dfNew.withColumn(item, missingWithModeValuesUDF(col(item), lit(mode_value)))
        except:
            dfNew = dfNew.na.fill(0, [item])
    
    return dfNew','fillNullWithModValue',1);
   
   
   
   
   
	
	
	
	
	
	
	
	
	
