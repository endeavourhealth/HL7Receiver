
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