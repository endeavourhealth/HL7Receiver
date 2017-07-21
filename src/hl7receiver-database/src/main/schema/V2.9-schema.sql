/* 
	Schema V2.9: Create message type option tables
*/
create table configuration.message_type_option_type
(
	message_type_option_type varchar(100) not null,
	description varchar(1000) not null,
	
	constraint configuration_messagetypeoptiontype_messagetypeoptiontype_pk primary key (message_type_option_type),
	constraint configuration_messagetypeoptiontype_messagetypeoptiontype_ck check (char_length(trim(message_type_option_type)) > 0)
);

insert into configuration.message_type_option_type
(
	message_type_option_type,
	description
)
values
(
	'CheckPid1NotBlank', 
	'At message receipt, ensures the patient identifier value is not blank for pid1 field as defined in configuration.channel.  If blank an AE message code is returned.'
),
(
	'CheckPid2NotBlank', 
	'At message receipt, ensures the patient identifier value is not blank for pid2 field as defined in configuration.channel.  If blank an AE message code is returned.'
),
(
	'CheckMrgSegmentField5NotBlank',
	'At message receipt, ensures MRG segment field 5 (Prior visit number) field is not blank.  If blank an AE message code is returned.'
);

create table configuration.channel_message_type_option
(
	channel_id integer not null,
	message_type varchar(100) not null,
	message_type_option_type varchar(100) not null,
	message_type_option_value varchar(100) not null,
	
	constraint configuration_channelmessagetypeoption_channelid_messagetype_messagetypeoptiontype_pk primary key (channel_id, message_type, message_type_option_type),
	constraint configuration_channelmessagetypeoption_channelid_fk foreign key (channel_id) references configuration.channel (channel_id),
	constraint configuration_channelmessagetypeoption_channelid_messagetype_fk foreign key (channel_id, message_type) references configuration.channel_message_type (channel_id, message_type),
	constraint configuration_channelmessagetypeoption_messagetypeoptiontype_fk foreign key (message_type_option_type) references configuration.message_type_option_type (message_type_option_type)
);
