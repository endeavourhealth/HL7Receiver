/* 
	create schemas
*/
create schema mapping;

/*
	create mapping.resource_uuid table
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

/*
	create and populate mapping.code_origin
*/
create table mapping.code_origin
(
	code_origin_id char(1) not null,
	code_origin_name varchar(100) not null,
	code_origin_description varchar(1000) not null,
	hl7_channel_id integer null,
	
	constraint mapping_codeorigin_codeoriginid_pk primary key (code_origin_id),
	constraint mapping_codeorigin_codeoriginname_uq unique (code_origin_name),
	constraint mapping_codeorigin_codeoriginname_ck check (char_length(trim(code_origin_name)) > 0),
	constraint mapping_codeorigin_hl7channelid_fk foreign key (hl7_channel_id) references configuration.channel (channel_id),
	constraint mapping_codeorigin_hl7channelid_uq unique (hl7_channel_id)
);

insert into mapping.code_origin
(
	code_origin_id,
	code_origin_name,
	code_origin_description,
	hl7_channel_id
)
values
('G', 'GLOBAL', 'Global', null),
('H', 'HOMERTON', 'Homerton ADT feed', 1);

/*
	create and populate mapping.code_action table
*/
create table mapping.code_action
(
	code_action_id char(1) not null,
	code_action_name varchar(100) not null,
	code_action_description varchar(1000) not null,
	
	constraint mapping_codeaction_codeactionid_pk primary key (code_action_id),
	constraint mapping_codeaction_codeactionname_uq unique (code_action_name),
	constraint mapping_codeaction_codeactionname_ck check (char_length(trim(code_action_name)) > 0)
);

insert into mapping.code_action
(
	code_action_id,
	code_action_name,
	code_action_description
)
values
('F', 'Fail transformation', 'Fail transformation of message'),
('X', 'Exclude', 'Exclude (source or target) code, system and term from transformed message'),
('S', 'Include only source term', 'Include only the source term in the transformed message'),
('T', 'Include', 'Include the target code, system and term in the transformed message');

/*
	create and populate mapping.code_context table
*/
create table mapping.code_context
(
	code_context_id integer not null,
	code_context_short_name varchar(100) not null,
	code_action_id_unmapped_default char(1) not null,
	message_type varchar(100) not null,
	field_locator varchar(100) not null,
	code_context_description varchar(1000) not null,
	
	constraint mapping_codecontext_contextid_pk primary key (code_context_id),
	constraint mapping_codecontext_codecontextshortname_uq unique (code_context_short_name),
	constraint mapping_codecontext_codecontextshortname_ck check (char_length(trim(code_context_short_name)) > 0),
	constraint mapping_codecontext_codecontextshortname_ck2 check (upper(code_context_short_name) = code_context_short_name),
	constraint mapping_codecontext_codeactionidunmappeddefault_fk foreign key (code_action_id_unmapped_default) references mapping.code_action (code_action_id)
);

insert into mapping.code_context
(
	code_context_id,
	code_context_short_name,
	code_action_id_unmapped_default,
	message_type,
	field_locator,
	code_context_description
)
values
(1, 'HL7_PRIMARY_LANGUAGE', 			'F',	'HL7 ADT', 'PID.15', 'Patient primary language (HL7 v2)'),
(2, 'HL7_TELECOM_USE_CODE', 			'F',	'HL7 ADT', 'XTN.2', 'Telecom use code (HL7 v2)'),
(3, 'HL7_TELECOM_EQUIPMENT_TYPE', 	'F', 	'HL7 ADT', 'XTN.3', 'Telecom equipment type (HL7 v2)');

/*
	create and populate mapping.code_system table
*/
create table mapping.code_system
(
	code_system_id integer not null,
	code_system_name varchar(100) not null,
	code_system_identifier varchar(500) not null,
	code_system_description varchar(100) not null,
	code_system_examples varchar(100) not null,
	
	constraint mapping_codesystem_codesystemid_pk primary key (code_system_id),
	constraint mapping_codesystem_codesystemname_uq unique (code_system_name),
	constraint mapping_codesystem_codesystemname_ck check (char_length(trim(code_system_name)) > 0),
	constraint mapping_codesystem_codesystemidentifier_uq unique (code_system_identifier),
	constraint mapping_codesystem_codesystemidentifier_ck check (char_length(trim(code_system_identifier)) > 0)
);

insert into mapping.code_system
(
	code_system_id,
	code_system_name,
	code_system_identifier,
	code_system_description,
	code_system_examples
)
values
(
	1,
	'Human language (FHIR)',
	'http://fhir.nhs.net/ValueSet/human-language-1',
	'See http://www.datadictionary.nhs.uk/data_dictionary/attributes/l/language_code_de.asp',
	'en, fr, de, q1'
),
(
	2,
	'Contact point system (FHIR)',
	'http://hl7.org/fhir/contact-point-system',
	'See http://hl7.org/fhir/DSTU2/valueset-contact-point-system.html',
	'phone, fax, email'
),
(
	3,
	'Contact point use (FHIR)',
	'http://hl7.org/fhir/contact-point-use',
	'See http://hl7.org/fhir/DSTU2/valueset-contact-point-use.html',
	'home, work, temp, mobile'
);


/*
	create mapping.code table
*/
create table mapping.code
(
	code_id serial not null,
	source_code_origin_id char(1) not null,
	source_code_context_id integer not null,
	source_code varchar(100) not null,
	source_code_system_id integer null,
	source_term varchar(500) not null,
	is_mapped boolean not null,
	target_code varchar(100) null,
	target_code_system_id integer null,
	target_term varchar(500) null,
	target_code_action_id char(1) not null,
	
	constraint mapping_code_codeid_pk primary key (code_id),
	constraint mapping_code_sccodeoriginid_sccontextid_sc_sccodesystem_sterm_pk unique (source_code_origin_id, source_code_context_id, source_code, source_code_system_id, source_term),
	constraint mapping_code_sourcecodeoriginid_fk foreign key (source_code_origin_id) references mapping.code_origin (code_origin_id),
	constraint mapping_code_sourcecodecontextid_fk foreign key (source_code_context_id) references mapping.code_context (code_context_id),
	constraint mapping_code_sourcecodesystemid_fk foreign key (source_code_system_id) references mapping.code_system (code_system_id),
	constraint mapping_code_ismapped_targetcode_targetcodesystemid_targetterm_ck check (((not is_mapped) and (target_code is null and target_code_system_id is null and target_term is null)) or (is_mapped)),
	constraint mapping_code_targetcodesystemid_fk foreign key (target_code_system_id) references mapping.code_system (code_system_id),
	constraint mapping_code_targetcodeactionid_fk foreign key (target_code_action_id) references mapping.code_action (code_action_id)
);
