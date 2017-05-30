
create or replace function mapping.get_resource_uuid
(
	_channel_id integer,
	_resource_type varchar(100),
	_unique_identifier varchar(200)
)
returns uuid
as $$
declare
	_resource_uuid uuid;
begin
	
	insert into mapping.resource_uuid
	(
		channel_id,
		resource_type,
		unique_identifier,
		resource_uuid
	)
	values
	(
		_channel_id,
		_resource_type,
		_unique_identifier,
		uuid_generate_v4()
	)
	on conflict on constraint mapping_resourceuuid_channelid_resourcetype_uniqueidentifier_pk
	do nothing;

	select 
		resource_uuid into _resource_uuid
	from mapping.resource_uuid
	where channel_id = _channel_id
	and resource_type = _resource_type
	and unique_identifier = _unique_identifier;

	if (_resource_uuid is null)
	then
		raise exception 'resource uuid could not be generated for channel % resource % identifier %', _channel_id, _resource_type, _unique_identifier;
		return null;
	end if;
	
	return _resource_uuid;
	
end;
$$ language plpgsql;
