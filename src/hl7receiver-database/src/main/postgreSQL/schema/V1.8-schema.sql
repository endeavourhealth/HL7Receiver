/*
	Schema V1.8: Change mapping.code_origin to mapping.scope
*/

create table mapping.scope
(
	scope_id char(1) not null,
	scope_name varchar(100) not null,
	scope_description varchar(1000) not null,
	hl7_channel_id integer null,
	
	constraint mapping_scope_scopeid_pk primary key (scope_id),
	constraint mapping_scope_scopename_uq unique (scope_name),
	constraint mapping_scope_scopename_ck check (char_length(trim(scope_name)) > 0),
	constraint mapping_scope_scopename_ck2 check (upper(scope_name) = scope_name),
	constraint mapping_scope_hl7channelid_fk foreign key (hl7_channel_id) references configuration.channel (channel_id),
	constraint mapping_scope_hl7channelid_uq unique (hl7_channel_id)
);

insert into mapping.scope
(
	scope_id,
	scope_name,
	scope_description,
	hl7_channel_id
)
select 
	o.code_origin_id,
	o.code_origin_name,
	o.code_origin_description,
	o.hl7_channel_id
from mapping.code_origin o;

alter table mapping.code rename source_code_origin_id to scope_id;
alter table mapping.code drop constraint mapping_code_sourcecodeoriginid_fk;

alter table mapping.code add constraint mapping_code_scopeid_fk foreign key (scope_id) references mapping.scope (scope_id);

drop table mapping.code_origin;
