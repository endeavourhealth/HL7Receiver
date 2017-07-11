/* 
	Schema V2.6: Move dictionary.channel_option_type into the configuration schema
*/

create table configuration.channel_option_type
(
	channel_option_type varchar(100) not null,
	default_value varchar(100) not null,
	description varchar(1000) not null,
	
	constraint configuration_channeloptiontype_channeloptiontype_pk primary key (channel_option_type),
	constraint configuration_channeloptiontype_channeloptiontype_ck check (char_length(trim(channel_option_type)) > 0)
);

insert into configuration.channel_option_type
(
	channel_option_type,
	default_value,
	description
)
select
	channel_option_type,
	default_value,
	description
from dictionary.channel_option_type;

alter table configuration.channel_option drop constraint configuration_channeloption_channeloptiontypeid_fk;

alter table configuration.channel_option add constraint configuration_channeloption_channeloptiontype_fk foreign key (channel_option_type) references configuration.channel_option_type (channel_option_type);

drop table dictionary.channel_option_type;
