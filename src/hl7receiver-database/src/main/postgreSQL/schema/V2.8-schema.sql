/* 
	Schema V2.8: Move dictionary.message_type and dictionary.message_status into log schema
*/

create table configuration.message_type
(
	message_type varchar(100) not null,
	description varchar(100) not null,
	
	constraint configuration_messagetype_messagetype_pk primary key (message_type),
	constraint configuration_messagetype_messagetype_ck check (char_length(trim(message_type)) > 0),
	constraint configuration_messagetype_description_ck check (char_length(trim(description)) > 0)
);

insert into configuration.message_type
(
	message_type,
	description
)
select
	message_type,
	description
from dictionary.message_type;

alter table configuration.channel_message_type drop constraint configuration_channelmessagetype_messagetype_fk;
alter table configuration.channel_message_type add constraint configuration_channelmessagetype_messagetype_fk foreign key (message_type) references configuration.message_type (message_type);

drop table dictionary.message_type;

create table log.message_status
(
	message_status_id smallint not null,
	is_complete boolean not null,
	description varchar(100) not null,
	
	constraint log_messagestatus_messagestatusid_pk primary key (message_status_id),
	constraint log_messagestatus_messagestatusid_iscomplete_uq unique (message_status_id, is_complete),
	constraint log_messagestatus_description_uq unique (description),
	constraint log_messagestatus_description_ck check (char_length(trim(description)) > 0)
);

insert into log.message_status
(
	message_status_id,
	is_complete,
	description
)
select
	message_status_id,
	is_complete,
	description
from dictionary.message_status;

alter table log.message drop constraint log_message_messagestatusid_fk;

alter table log.message_status_history drop constraint log_messagestatushistory_messagestatusid_iscomplete_fk;
alter table log.message_status_history add constraint log_messagestatushistory_messagestatusid_iscomplete_fk foreign key (message_status_id, is_complete) references log.message_status (message_status_id, is_complete);

alter table log.message drop constraint log_messagestatus_messagestatusid_iscomplete_fk;
alter table log.message add constraint log_messagestatus_messagestatusid_iscomplete_fk foreign key (message_status_id, is_complete) references log.message_status (message_status_id, is_complete);

drop table dictionary.message_status;

drop table dictionary.acknowledgement_code;

drop schema dictionary;
