/*
	create extensions
*/
create extension "uuid-ossp";

/* 
	create schemas
*/
create schema dictionary;
create schema configuration;
create schema log;
create schema helper;

/*
	create tables - dictionary
*/
create table dictionary.message_type
(
	message_type varchar(100) not null,
	description varchar(100) not null,
	
	constraint dictionary_messagetype_messagetype_pk primary key (message_type),
	constraint dictionary_messagetype_messagetype_ck check (char_length(trim(message_type)) > 0),
	constraint dictionary_messagetype_description_ck check (char_length(trim(description)) > 0)
);

create table dictionary.channel_option_type
(
	channel_option_type varchar(100) not null,
	default_value varchar(100) not null,
	description varchar(1000) not null,
	
	constraint dictionary_channeloptiontype_channeloptiontype_pk primary key (channel_option_type),
	constraint dictionary_channeloptiontype_channeloptiontype_ck check (char_length(trim(channel_option_type)) > 0)
);

create table dictionary.message_status
(
	message_status_id smallint not null,
	is_complete boolean not null,
	description varchar(100) not null,
	
	constraint dictionary_messagestatus_messagestatusid_pk primary key (message_status_id),
	constraint dictionary_messagestatus_messagestatusid_iscomplete_uq unique (message_status_id, is_complete),
	constraint dictionary_messagestatus_description_uq unique (description),
	constraint dictionary_messagestatus_description_ck check (char_length(trim(description)) > 0)
);

create table dictionary.processing_content_type
(
	processing_content_type_id smallint not null,
	description varchar(100) not null,
	
	constraint dictionary_processingcontenttype_processingcontenttypeid_pk primary key (processing_content_type_id),
	constraint dictionary_processingcontenttype_description_uq unique (description),
	constraint dictionary_processingcontenttype_description_ck check (char_length(trim(description)) > 0)
);

/*
	create tables - configuration
*/
create table configuration.channel
(
	channel_id integer not null,
	channel_name varchar(100) not null,
	port_number integer not null,
	is_active boolean not null,
	use_tls boolean not null,
	sending_application varchar(100) not null,
	sending_facility varchar(100) not null,
	receiving_application varchar(100) not null,
	receiving_facility varchar(100) not null,
	pid1_field integer null,
	pid1_assigning_auth varchar(100) null,
	pid2_field integer null,
	pid2_assigning_auth varchar(100) null,
	eds_service_identifier varchar(100) not null,
	notes varchar(1000) not null,

	constraint configuration_channel_channelid_pk primary key (channel_id),
	constraint configuration_channel_channelname_uq unique (channel_name),
	constraint configuration_channel_portnumber_uq unique (port_number),
	constraint configuration_channel_portnumber_ck check (port_number > 0),
	constraint configuration_channel_channelname_ck check (char_length(trim(channel_name)) > 0),
	constraint configuration_channel_pid1field_ck check ((pid1_field >= 1 and pid1_field <= 30) or (pid1_field is null)),
	constraint configuration_channel_pid2field_ck check ((pid2_field >= 1 and pid2_field <= 30) or (pid2_field is null)),
	constraint configuration_channel_pid1field_pid1assigningauth_ck check ((pid1_assigning_auth is null) or ((pid1_field is not null) and (pid1_assigning_auth is not null))),
	constraint configuration_channel_pid2field_pid2assigningauth_ck check ((pid2_assigning_auth is null) or ((pid2_field is not null) and (pid2_assigning_auth is not null))),
	constraint configuration_eds_edsserviceidentifier_uq unique (eds_service_identifier),
	constraint configuration_eds_edsserviceidentifier_ck check (char_length(trim(eds_service_identifier)) > 0)
);

create table configuration.channel_message_type
(
	channel_id integer not null,
	message_type varchar(100) not null,
	is_allowed boolean not null,
	
	constraint configuration_channelmessagetype_channelid_messagetype_pk primary key (channel_id, message_type),
	constraint configuration_channelmessagetype_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint configuration_channelmessagetype_messagetype_fk foreign key (message_type) references dictionary.message_type (message_type)
);

create table configuration.channel_option
(
	channel_id integer not null,
	channel_option_type varchar(100) not null,
	channel_option_value varchar(100) not null,
	
	constraint configuration_channeloption_channelid_channeloptiontype_pk primary key (channel_id, channel_option_type),
	constraint configuration_channeloption_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint configuration_channeloption_channeloptiontypeid_fk foreign key (channel_option_type) references dictionary.channel_option_type (channel_option_type)
);

create table configuration.eds
(
	single_row_lock boolean,
	eds_url varchar(1000) not null,
	software_content_type varchar(100) not null,
	software_version varchar(100) not null,
	use_keycloak boolean not null,
	keycloak_token_uri varchar(500) null,
	keycloak_realm varchar(100) null,
	keycloak_username varchar(100) null,
	keycloak_password varchar(100) null,
	keycloak_clientid varchar(100) null,

	constraint configuration_eds_singlerowlock_pk primary key (single_row_lock),
	constraint configuration_eds_singlerowlock_ck check (single_row_lock = true),
	constraint configuration_eds_edsurl_ck check (char_length(trim(eds_url)) > 0),
	constraint configuration_eds_softwarecontenttype_ck check (char_length(trim(software_content_type)) > 0),
	constraint configuration_eds_softwareversion_ck check (char_length(trim(software_version)) > 0),
	constraint configuration_configurationeds_usekeycloak_keycloaktokenuri_keycloakrealm_keycloakusername_keycloakpassword_keycloakclientid_ck check ((not use_keycloak) or (keycloak_token_uri is not null and keycloak_realm is not null and keycloak_username is not null and keycloak_password is not null and keycloak_clientid is not null)),
	constraint configuration_configurationeds_keycloaktokenuri_ck check (keycloak_token_uri is null or (char_length(trim(keycloak_token_uri)) > 0)),
	constraint configuration_configurationeds_keycloakrealm_ck check (keycloak_realm is null or (char_length(trim(keycloak_realm)) > 0)),
	constraint configuration_configurationeds_keycloakusername_ck check (keycloak_username is null or (char_length(trim(keycloak_username)) > 0)),
	constraint configuration_configurationeds_keycloakpassword_ck check (keycloak_password is null or (char_length(trim(keycloak_password)) > 0)),
	constraint configuration_configurationeds_keycloakclientid_ck check (keycloak_clientid is null or (char_length(trim(keycloak_clientid)) > 0))
);

create table configuration.processing_attempt_interval
(
	processing_attempt_id smallint not null,
	interval_seconds integer not null,
	
	constraint configuration_processingattemptinterval_processingattemptid_pk primary key (processing_attempt_id),
	constraint configuration_processingattemptinterval_intervalseconds_ck check (interval_seconds >= 0)
);

/*
	create tables - log
*/
create table log.instance
(
	instance_id integer not null,
	hostname varchar(100) not null,
	added_date timestamp not null,
	last_get_config_date timestamp not null,

	constraint log_instance_instanceid_pk primary key (instance_id),
	constraint log_instance_hostname_uq unique (hostname), 
	constraint log_instance_hostname_ck check (char_length(trim(hostname)) > 0)
);

create table log.connection
(
	connection_id serial not null,
	instance_id integer not null,
	channel_id integer not null,
	local_port integer not null,
	remote_host varchar(100) not null,
	remote_port integer not null,
	connect_date timestamp not null,
	disconnect_date timestamp null,
	
	constraint log_connection_connectionid_pk primary key (connection_id),
	constraint log_connection_instanceid_fk foreign key (instance_id) references log.instance (instance_id),
	constraint log_connection_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_connection_channelid_connectionid_uq unique (channel_id, connection_id),
	constraint log_connection_localport_ck check (local_port > 0),
	constraint log_connection_remotehost_ck check (char_length(trim(remote_host)) > 0), 
	constraint log_connection_remoteport_ck check (remote_port > 0),
	constraint log_connection_connectdate_disconnectdate_ck check ((disconnect_date is null) or (connect_date <= disconnect_date)) 
);

create table log.message
(
	message_id serial not null,
	channel_id integer not null,
	connection_id integer not null,
	log_date timestamp not null,
	message_control_id varchar(100) not null,
	message_sequence_number varchar(100) null,
	message_date timestamp not null,
	pid1 varchar(100) null,
	pid2 varchar(100) null,
	inbound_message_type varchar(100) not null,
	inbound_payload varchar not null,
	outbound_message_type varchar(100) not null,
	outbound_payload varchar not null,
	message_uuid uuid not null,
	message_status_id smallint not null,
	message_status_date timestamp not null,
	processing_attempt_id smallint not null,
	next_attempt_date timestamp null,
	
	constraint log_message_messageid_pk primary key (message_id),
	constraint log_message_messageid_channelid_uq unique (message_id, channel_id),
	constraint log_message_channelid_connectionid_fk foreign key (channel_id, connection_id) references log.connection (channel_id, connection_id),
	constraint log_message_inboundmessagetype_fk foreign key (channel_id, inbound_message_type) references configuration.channel_message_type (channel_id, message_type),
	constraint log_message_outboundmessagetype_fk foreign key (channel_id, outbound_message_type) references configuration.channel_message_type (channel_id, message_type),
	constraint log_message_messagecontrolid_ck check (char_length(trim(message_control_id)) > 0),
	constraint log_message_messageuuid_uq unique (message_uuid),
	constraint log_message_messagestatusid_fk foreign key (message_status_id) references dictionary.message_status (message_status_id),
	constraint log_message_processingattemptid_ck check (processing_attempt_id >= 0)
);

create table log.message_status_history
(
	message_status_history_id serial not null,
	message_id integer not null,
	processing_attempt_id smallint not null,
	message_status_id smallint not null,
	message_status_date timestamp not null,
	is_complete boolean not null,
	error_message text null,
	instance_id integer not null,
	
	constraint log_messagestatushistory_messagestatushistoryid_pk primary key (message_status_history_id),
	constraint log_messagestatushistory_messageid_fk foreign key (message_id) references log.message (message_id), 
	constraint log_messagestatushistory_processingattemptid_ck check (processing_attempt_id >= 0),
	constraint log_messagestatushistory_messagestatusid_iscomplete_fk foreign key (message_status_id, is_complete) references dictionary.message_status (message_status_id, is_complete),
	constraint log_messagestatushistory_iscomplete_errormessage_ck check ((is_complete and error_message is null) or (not is_complete)),
	constraint log_messagestatushistory_instanceid_fk foreign key (instance_id) references log.instance (instance_id)
);

create table log.message_queue
(
	message_id integer not null,
	channel_id integer not null,
	message_date timestamp not null,
	log_date timestamp not null,

	constraint log_messagequeue_messageid_pk primary key (message_id),
	constraint log_messagequeue_messageid_channelid_fk foreign key (message_id, channel_id) references log.message (message_id, channel_id)
);

create index concurrently log_messagequeue_messagedate_logdate_ix on log.message_queue (message_date, log_date);

create table log.message_processing_content
(
	message_processing_content_id serial not null,
	message_id integer not null,
	processing_attempt_id smallint not null,
	content_saved_date timestamp not null,
	processing_content_type_id smallint not null,
	content text not null,
	
	constraint log_messageprocessingcontent_messageprocessingcontentid_pk primary key (message_processing_content_id),
	constraint log_messageprocessingcontent_messageid_fk foreign key (message_id) references log.message (message_id),
	constraint log_messageprocessingcontent_processingcontenttypeid_fk foreign key (processing_content_type_id) references dictionary.processing_content_type (processing_content_type_id)
);

create table log.dead_letter
(
	dead_letter_id serial not null,
	instance_id integer null,
	channel_id integer null,
	connection_id integer null,
	log_date timestamp not null,
	local_host varchar(100) null,
	local_port integer null,
	remote_host varchar(100) null,
	remote_port integer null,
	sending_application varchar(100) null,
	sending_facility varchar(100) null,
	receiving_application varchar(100) null,
	receiving_facility varchar(100) null,
	message_control_id varchar(100) null,
	message_sequence_number varchar(100) null,
	message_date timestamp null,
	pid1 varchar(100) null,
	pid2 varchar(100) null,
	inbound_message_type varchar(100) null,
	inbound_payload varchar not null,
	outbound_message_type varchar(100) null,
	outbound_payload varchar null,
	exception varchar not null,
	dead_letter_uuid varchar not null,	
	
	constraint log_deadletter_deadletterid_pk primary key (dead_letter_id),
	constraint log_deadletter_instanceid_fk foreign key (instance_id) references log.instance (instance_id),
	constraint log_deadletter_connectionid_fk foreign key (connection_id) references log.connection (connection_id),
	constraint log_deadletter_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_deadletter_deadletteruuid_uq unique (dead_letter_uuid)
);

create table log.error_digest
(
	error_digest_id integer not null,
	error_count integer not null,
	last_log_date timestamp not null,
	log_class varchar(1000) not null,
	log_method varchar(1000) not null,
	log_message varchar(1000) not null,
	exception varchar not null,
	
	constraint log_errordigest_errordigestid_pk primary key (error_digest_id),
	constraint log_errordigest_errorcount_ck check (error_count > 0),
	constraint log_errordigest_logclass_logmethod_logmessage_exception_uq unique (log_class, log_method, log_message, exception)
);

create table log.channel_processor_lock
(
	channel_id integer not null,
	instance_id integer not null,
	heartbeat_date timestamp not null,
	
	constraint log_channelprocessorlock_channelid_pk primary key (channel_id),
	constraint log_channelprocessorlock_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint log_channelprocessorlock_instanceid_fk foreign key (instance_id) references log.instance (instance_id)
);

/*
	insert data
*/
insert into dictionary.message_type (message_type, description) values
('ADT^A01', 'Admit / visit notification'),
('ADT^A02', 'Transfer a patient'),
('ADT^A03', 'Discharge/end visit'),
('ADT^A04', 'Register a patient'),
('ADT^A05', 'Pre-admit a patient'),
('ADT^A06', 'Change an outpatient to an inpatient'),
('ADT^A07', 'Change an inpatient to an outpatient'),
('ADT^A08', 'Update patient information'),
('ADT^A09', 'Patient departing - tracking'),
('ADT^A10', 'Patient arriving - tracking'),
('ADT^A11', 'Cancel admit/visit notification'),
('ADT^A12', 'Cancel transfer'),
('ADT^A13', 'Cancel discharge/end visit'),
('ADT^A14', 'Pending admit'),
('ADT^A15', 'Pending transfer'),
('ADT^A16', 'Pending discharge'),
('ADT^A17', 'Swap patients'),
('ADT^A18', 'Merge patient information'),
('ADT^A19', 'Patient query'),
('ADT^A20', 'Bed status update'),
('ADT^A21', 'Patient goes on a "leave of absence"'),
('ADT^A22', 'Patient returns from a "leave of absence"'),
('ADT^A23', 'Delete a patient record'),
('ADT^A24', 'Link patient information'),
('ADT^A25', 'Cancel pending discharge'),
('ADT^A26', 'Cancel pending transfer'),
('ADT^A27', 'Cancel pending admit'),
('ADT^A28', 'Add person information'),
('ADT^A29', 'Delete person information'),
('ADT^A30', 'Merge person information'),
('ADT^A31', 'Update person information'),
('ADT^A32', 'Cancel patient arriving - tracking'),
('ADT^A33', 'Cancel patient departing - tracking'),
('ADT^A34', 'Merge patient information - patient ID only'),
('ADT^A35', 'Merge patient information - account number only'),
('ADT^A36', 'Merge patient information - patient ID and account number'),
('ADT^A37', 'Unlink patient information'),
('ADT^A38', 'Cancel pre-admit'),
('ADT^A39', 'Merge person - external ID'),
('ADT^A40', 'Merge patient - internal ID'),
('ADT^A41', 'Merge account - patient account number'),
('ADT^A42', 'Merge visit - visit number'),
('ADT^A43', 'Move patient information - internal ID'),
('ADT^A44', 'Move account information - patient account number'),
('ADT^A45', 'Move visit information - visit number'),
('ADT^A46', 'Change external ID'),
('ADT^A47', 'Change internal ID'),
('ADT^A48', 'Change alternate patient ID'),
('ADT^A49', 'Change patient account number'),
('ADT^A50', 'Change visit number'),
('ADT^A51', 'Change alternate visit ID'),
('ACK^A01', 'Acknowledgement to admit / visit notification'),
('ACK^A02', 'Acknowledgement to transfer a patient'),
('ACK^A03', 'Acknowledgement to discharge/end visit'),
('ACK^A04', 'Acknowledgement to register a patient'),
('ACK^A05', 'Acknowledgement to pre-admit a patient'),
('ACK^A06', 'Acknowledgement to change an outpatient to an inpatient'),
('ACK^A07', 'Acknowledgement to change an inpatient to an outpatient'),
('ACK^A08', 'Acknowledgement to update patient information'),
('ACK^A09', 'Acknowledgement to patient departing - tracking'),
('ACK^A10', 'Acknowledgement to patient arriving - tracking'),
('ACK^A11', 'Acknowledgement to cancel admit/visit notification'),
('ACK^A12', 'Acknowledgement to cancel transfer'),
('ACK^A13', 'Acknowledgement to cancel discharge/end visit'),
('ACK^A14', 'Acknowledgement to pending admit'),
('ACK^A15', 'Acknowledgement to pending transfer'),
('ACK^A16', 'Acknowledgement to pending discharge'),
('ACK^A17', 'Acknowledgement to swap patients'),
('ACK^A18', 'Acknowledgement to merge patient information'),
('ACK^A19', 'Acknowledgement to patient query'),
('ACK^A20', 'Acknowledgement to bed status update'),
('ACK^A21', 'Acknowledgement to patient goes on a "leave of absence"'),
('ACK^A22', 'Acknowledgement to patient returns from a "leave of absence"'),
('ACK^A23', 'Acknowledgement to delete a patient record'),
('ACK^A24', 'Acknowledgement to link patient information'),
('ACK^A25', 'Acknowledgement to cancel pending discharge'),
('ACK^A26', 'Acknowledgement to cancel pending transfer'),
('ACK^A27', 'Acknowledgement to cancel pending admit'),
('ACK^A28', 'Acknowledgement to add person information'),
('ACK^A29', 'Acknowledgement to delete person information'),
('ACK^A30', 'Acknowledgement to merge person information'),
('ACK^A31', 'Acknowledgement to update person information'),
('ACK^A32', 'Acknowledgement to cancel patient arriving - tracking'),
('ACK^A33', 'Acknowledgement to cancel patient departing - tracking'),
('ACK^A34', 'Acknowledgement to merge patient information - patient ID only'),
('ACK^A35', 'Acknowledgement to merge patient information - account number only'),
('ACK^A36', 'Acknowledgement to merge patient information - patient ID and account number'),
('ACK^A37', 'Acknowledgement to unlink patient information'),
('ACK^A38', 'Acknowledgement to cancel pre-admit'),
('ACK^A39', 'Acknowledgement to merge person - external ID'),
('ACK^A40', 'Acknowledgement to merge patient - internal ID'),
('ACK^A41', 'Acknowledgement to merge account - patient account number'),
('ACK^A42', 'Acknowledgement to merge visit - visit number'),
('ACK^A43', 'Acknowledgement to move patient information - internal ID'),
('ACK^A44', 'Acknowledgement to move account information - patient account number'),
('ACK^A45', 'Acknowledgement to move visit information - visit number'),
('ACK^A46', 'Acknowledgement to change external ID'),
('ACK^A47', 'Acknowledgement to change internal ID'),
('ACK^A48', 'Acknowledgement to change alternate patient ID'),
('ACK^A49', 'Acknowledgement to change patient account number'),
('ACK^A50', 'Acknowledgement to change visit number'),
('ACK^A51', 'Acknowledgement to change alternate visit ID');

insert into dictionary.message_status
(
	message_status_id,
	is_complete,
	description
)
values
(0, false, 'Message received'),
(1, false, 'Message processing started'),
(9, true, 'Message processing complete'),
(-1, false, 'Transform failure'),
(-2, false, 'Envelope generation failure'),
(-3, false, 'Send failure'),
(-9, false, 'Unexpected error');

insert into dictionary.processing_content_type
(
	processing_content_type_id,
	description
)
values
(1, 'FHIR representation'),
(2, 'Onward request message'),
(3, 'Onward response message');

insert into dictionary.channel_option_type
(
	channel_option_type,
	default_value,
	description
)
values
('KeepOnlyCurrentMessageProcessingContentAttempt', 'FALSE', 'Set to TRUE to remove old entries in log.message_processing_content'),
('MaxSkippableProcessingErroredMessages', '0', 'Number of messages in processing error before new messages processing halts (until retries are successful)'),
('SkipOnwardMessageSendingInProcessor', 'FALSE', 'Don''t send messages onward in processor (to test transform and mapping functionality'),
('PauseProcessor', 'FALSE', 'Pause processing of messages');

insert into configuration.processing_attempt_interval 
(
	processing_attempt_id, 
	interval_seconds
) 
values 
(1, 0),			-- 0 seconds
(2, 15), 		-- 15 seconds
(3, 30), 		-- 30 seconds
(4, 120), 		-- 2 minutes
(5, 600),  	-- 10 minutes
(6, 3600), 	-- 1 hour
(7, 7200), 	-- 2 hours
(8, 14400)		-- 4 hours
