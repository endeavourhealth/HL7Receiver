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

create table mapping.code_origin
(
	code_origin_id char(1) not null,
	code_origin_name varchar(100) not null,
	code_origin_description varchar(1000) not null,
	hl7_channel_id integer null,
	
	constraint mapping_codeorigin_codeoriginid_pk primary key (code_origin_id),
	constraint mapping_codeorigin_codeoriginname_uq unique (code_origin_name),
	constraint mapping_codeorigin_codeoriginname_ck check (char_length(trim(code_origin_name)) > 0),
	constraint mapping_codeorigin_codeoriginname_ck2 check (upper(code_origin_name) = code_origin_name),
	constraint mapping_codeorigin_hl7channelid_fk foreign key (hl7_channel_id) references configuration.channel (channel_id),
	constraint mapping_codeorigin_hl7channelid_uq unique (hl7_channel_id)
);

create table mapping.code_action
(
	code_action_id char(1) not null,
	is_mapped boolean not null,
	code_action_name varchar(100) not null,
	code_action_description varchar(1000) not null,
	
	constraint mapping_codeaction_codeactionid_pk primary key (code_action_id),
	constraint mapping_codeaction_codeactionid_ismapped_uq unique (code_action_id, is_mapped),
	constraint mapping_codeaction_codeactionname_uq unique (code_action_name),
	constraint mapping_codeaction_codeactionname_ck check (char_length(trim(code_action_name)) > 0)
);

create table mapping.code_context
(
	code_context_id integer not null,
	code_context_name varchar(100) not null,
	source_code_is_case_insensitive boolean not null,
	code_action_id_unmapped_default char(1) not null,
	message_type varchar(100) not null,
	field_locator varchar(100) not null,
	code_context_description varchar(1000) not null,
	
	constraint mapping_codecontext_contextid_pk primary key (code_context_id),
	constraint mapping_codecontext_codecontextname_uq unique (code_context_name),
	constraint mapping_codecontext_codecontextname_ck check (char_length(trim(code_context_name)) > 0),
	constraint mapping_codecontext_codecontextname_ck2 check (upper(code_context_name) = code_context_name),
	constraint mapping_codecontext_codeactionidunmappeddefault_fk foreign key (code_action_id_unmapped_default) references mapping.code_action (code_action_id)
);

create table mapping.code_system
(
	code_system_id integer not null,
	code_system_identifier varchar(500) not null,
	code_system_friendly_name varchar(100) not null,
	code_system_description varchar(500) not null,
	code_system_examples varchar(100) not null,
	
	constraint mapping_codesystem_codesystemid_pk primary key (code_system_id),
	constraint mapping_codesystem_codesystemidentifier_uq unique (code_system_identifier),
	constraint mapping_codesystem_codesystemidentifier_ck check (char_length(trim(code_system_identifier)) > 0),
	constraint mapping_codesystem_codesystemfriendlyname_uq unique (code_system_friendly_name),
	constraint mapping_codesystem_codesystemfriendlyname_ck check (char_length(trim(code_system_friendly_name)) > 0)
);

create table mapping.code
(
	code_id serial not null,
	source_code_origin_id char(1) not null,
	source_code_context_id integer not null,
	source_code varchar(100) not null,
	source_code_system_id integer not null,
	source_term varchar(500) not null,
	is_mapped boolean not null,
	target_code_action_id char(1) not null,
	target_code varchar(100) null,
	target_code_system_id integer null,
	target_term varchar(500) null,
	
	constraint mapping_code_codeid_pk primary key (code_id),
	constraint mapping_code_sccodeoriginid_sccontextid_sc_sccodesystem_sterm_pk unique (source_code_origin_id, source_code_context_id, source_code, source_code_system_id, source_term),
	constraint mapping_code_sourcecodeoriginid_fk foreign key (source_code_origin_id) references mapping.code_origin (code_origin_id),
	constraint mapping_code_sourcecodecontextid_fk foreign key (source_code_context_id) references mapping.code_context (code_context_id),
	constraint mapping_code_sourcecodesystemid_fk foreign key (source_code_system_id) references mapping.code_system (code_system_id),
	constraint mapping_code_ismapped_targetcode_targetcodesystemid_targetterm_ck check (((not is_mapped) and (target_code is null and target_code_system_id is null and target_term is null)) or ((is_mapped) and (target_code is not null and target_code_system_id is not null and target_term is not null))),
	constraint mapping_code_targetcodesystemid_fk foreign key (target_code_system_id) references mapping.code_system (code_system_id),
	constraint mapping_code_targetcodeactionid_fk foreign key (is_mapped, target_code_action_id) references mapping.code_action (is_mapped, code_action_id)
);

/*
	insert data
*/
insert into mapping.code_origin
(
	code_origin_id,
	code_origin_name,
	code_origin_description,
	hl7_channel_id
)
values
('G', 'GLOBAL', 'Global', null);

insert into mapping.code_action
(
	is_mapped,
	code_action_id,
	code_action_name,
	code_action_description
)
values
(false, 'F', 'Not mapped - fail transformation', 'The code is not mapped - fail transformation of message'),
(false, 'X', 'Not mapped - exclude', 'The code is not mapped - exclude the source code, system and term from transformed message'),
(false, 'S', 'Not mapped - include only source term', 'The code is not mapped - include only the source term in the transformed message'),
(true, 'T', 'Mapped - include', 'The code is mapped - include the target code, system and term in the transformed message');
