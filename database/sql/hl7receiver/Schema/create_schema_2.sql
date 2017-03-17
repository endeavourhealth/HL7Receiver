--drop schema mapping cascade;

/* 
	create schemas
*/
create schema mapping;

/*
	create tables - mapping
*/
create table mapping.resource_uuid
(
	channel_id integer not null,
	resource_type varchar(100) not null,
	unique_identifier varchar(200) not null,
	resource_uuid uuid not null,
	
	constraint mapping_resourceuuid_channelid_resourcetype_uniqueidentifier_pk primary key (channel_id, resource_type, unique_identifier),
	constraint mapping_resourceuuid_resourceuuid_uq unique (resource_uuid),
	constraint mapping_resourceuuid_channelid_fk foreign key (channel_id) references configuration.channel (channel_id)
);





create table mapping.code_set
(
	code_set_id integer not null,
	code_set_name varchar(100) not null,
	description varchar(1000) not null,
	
	constraint mapping_codeset_codesetid_pk primary key (code_set_id),
	constraint mapping_codeset_codesetname_uq unique (code_set_name),
	constraint mapping_codeset_codesetname_ck check (char_length(trim(code_set_name)) > 0)
);


create table mapping.code_context
(
	code_context_id integer not null,
	code_context_name varchar(100) not null,
	description varchar(1000) not null,
	
	constraint mapping_codecontext_contextid_pk primary key (code_context_id),
	constraint mapping_codecontext_contextname_uq unique (code_context_name),
	constraint mapping_codecontext_contextname_ck check (char_length(trim(code_context_name)) > 0),
	constraint mapping_codecontext_contextname_ck2 check (upper(code_context_name) = code_context_name)
);


create table mapping.code
(
	code_id serial not null,
	code_set_id integer not null,
	code_context_id integer not null,
	original_code varchar(100) not null,
	original_system varchar(100) not null,
	original_term varchar(1000) not null,
	is_mapped boolean not null constraint mapping_code_ismapped_df default (false),
	mapped_code varchar(100) null,
	mapped_system varchar(100) null,
	mapped_term varchar(100) null,
	
	constraint mapping_code_codeid_pk primary key (code_id),
	constraint mapping_code_codesetid_fk foreign key (code_set_id) references mapping.code_set (code_set_id),
	constraint mapping_code_codecontextid_fk foreign key (code_context_id) references mapping.code_context (code_context_id),
	constraint mapping_code_codesetid_codecontextid_originalcode_originalsystem_originalterm_uq unique (code_set_id, code_context_id, original_code, original_system, original_term),
	constraint mapping_code_ismapped_mappedcode_mappedsystem_mappedterm_ck check ((is_mapped and (mapped_code is not null and mapped_system is not null and mapped_term is not null)) or ((not is_mapped) and (mapped_code is null and mapped_system is null and mapped_term is null)))
);

insert into mapping.code_set
(
	code_set_id,
	code_set_name,
	description
)
values
(1, 'HL7ADT_ETHNICITY', '');


insert into mapping.code_context
(
	code_context_id,
	code_context_name,
	description
)
values
(1, 'GLOBAL', ''),
(2, 'HOMERTON', '');





















