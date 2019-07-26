
use hl7_receiver;


-- NOTE: the below MySQL script only creates a single MySQL table, which is enough to allow
-- us to run the Barts transform (which dips into the hl7 receiver DB)

CREATE TABLE resource_uuid
(
	scope_id char(1),
    resource_type varchar(50),
    unique_identifier varchar(200),
    resource_uuid char(36),
    CONSTRAINT pk_resource_uuid PRIMARY KEY (scope_id, resource_type, unique_identifier)
);



CREATE TABLE configuration_channel
(
    channel_id int NOT NULL,
    channel_name varchar(100),
    port_number int NOT NULL,
    is_active boolean NOT NULL,
    use_tls boolean NOT NULL,
    sending_application varchar(100),
    sending_facility varchar(100),
    receiving_application varchar(100),
    receiving_facility varchar(100),
    pid1_field int,
    pid1_assigning_auth varchar(100),
    pid2_field int,
    pid2_assigning_auth varchar(100),
    eds_service_identifier varchar(100) NOT NULL,
    notes varchar(1000) NOT NULL,
    CONSTRAINT pk_configuration_channel PRIMARY KEY (channel_id)
);

CREATE UNIQUE INDEX uix_channel_name ON configuration_channel (channel_name);
CREATE UNIQUE INDEX uix_port_number ON configuration_channel (port_number);
CREATE UNIQUE INDEX uix_eds_service_identifier ON configuration_channel (eds_service_identifier);


CREATE TABLE configuration_channel_option
(
    channel_id int NOT NULL,
    channel_option_type varchar(100) NOT NULL,
    channel_option_value varchar(100) NOT NULL,
    CONSTRAINT configuration_channeloption_channelid_channeloptiontype_pk PRIMARY KEY (channel_id, channel_option_type)
);

CREATE TABLE last_message
(
    message_id integer NOT NULL,
    channel_id integer NOT NULL,
    log_date timestamp,
    CONSTRAINT pk_last_message PRIMARY KEY (channel_id)
);

create table message
(
	message_id integer not null,
	channel_id integer not null,
	connection_id integer not null,
	log_date timestamp not null,
	message_control_id varchar(100) not null,
	message_sequence_number varchar(100) null,
	message_date timestamp not null,
	pid1 varchar(100) null,
	pid2 varchar(100) null,
	inbound_message_type varchar(100) not null,
	inbound_payload mediumtext not null,
	outbound_message_type varchar(100) not null,
	outbound_payload mediumtext null,
	message_uuid char(36) not null,
	message_status_id smallint not null,
	message_status_date timestamp not null,
	processing_attempt_id smallint not null,
	next_attempt_date timestamp null,
    error_message text,
	constraint log_message_messageid_pk primary key (message_id),
	constraint log_message_messageid_channelid_uq unique (message_id, channel_id)
);


CREATE TABLE message_queue
(
    message_id int NOT NULL,
    channel_id int NOT NULL,
    message_date timestamp NOT NULL,
    log_date timestamp NOT NULL,
    CONSTRAINT pk_message_queue PRIMARY KEY (message_id)
);

CREATE TABLE message_status_history
(
    message_status_history_id int NOT NULL,
    message_id int NOT NULL,
    processing_attempt_id smallint NOT NULL,
    message_status_id smallint NOT NULL,
    message_status_date timestamp NOT NULL,
    is_complete boolean NOT NULL,
    error_message text,
    instance_id int NOT NULL,
    CONSTRAINT pk_message_status_history PRIMARY KEY (message_status_history_id)
);

CREATE TABLE message_status
(
    message_status_id smallint NOT NULL,
    is_complete boolean NOT NULL,
    description varchar(100),
    CONSTRAINT pk_message_status PRIMARY KEY (message_status_id)
);

insert into message_status values (0, false, 'Message received');
insert into message_status values (1, false, 'Message processing started');
insert into message_status values (9, true, 'Message processing complete');
insert into message_status values (-1, false, 'Transform failure');
insert into message_status values (-2, false, 'Envelope generation failure');
insert into message_status values (-3, false, 'Send failure');
insert into message_status values (-9, false, 'Unexpected error');
