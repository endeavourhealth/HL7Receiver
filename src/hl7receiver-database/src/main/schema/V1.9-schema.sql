/*
	Schema V1.9: Change channel_id in mapping.resource_uuid to scope_id
*/

alter table mapping.resource_uuid drop constraint mapping_resourceuuid_channelid_fk;
alter table mapping.resource_uuid drop constraint mapping_resourceuuid_resourceuuid_uq;
alter table mapping.resource_uuid drop constraint mapping_resourceuuid_channelid_resourcetype_uniqueidentifier_pk;

alter table mapping.resource_uuid rename to resource_uuid_old;

create table mapping.resource_uuid
(
	scope_id char(1) not null,
	resource_type varchar(100) not null,
	unique_identifier varchar(200) not null,
	resource_uuid uuid not null,
	
	constraint mapping_resourceuuid_scopeid_resourcetype_uniqueidentifier_pk primary key (scope_id, resource_type, unique_identifier),
	constraint mapping_resourceuuid_resourceuuid_uq unique (resource_uuid),
	constraint mapping_resourceuuid_scopeid_fk foreign key (scope_id) references mapping.scope (scope_id)
);

insert into mapping.resource_uuid
(
	scope_id,
	resource_type,
	unique_identifier,
	resource_uuid
)
select
	s.scope_id,
	r.resource_type,
	r.unique_identifier,
	r.resource_uuid
from mapping.resource_uuid_old r
left outer join mapping.scope s on r.channel_id = s.hl7_channel_id;

drop table mapping.resource_uuid_old;
