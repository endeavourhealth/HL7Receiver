
create or replace function mapping.get_resource_uuid
(
	_scope_name varchar(100),
	_resource_type varchar(100),
	_unique_identifier varchar(200)
)
returns uuid
as $$
declare
	_scope_id char(1);
	_resource_uuid uuid;
begin
	
	--------------------------------------------
	-- lookup scope
	--
	_scope_name = trim(coalesce(_scope_name, ''));
	
	select
		scope_id into _scope_id
	from mapping.scope 
	where scope_name = _scope_name;
	
	if (_scope_id is null)
	then
		raise exception 'Could not find scope_name of %', _scope_name;
		return null;
	end if;

	--------------------------------------------
	-- insert mapping if not exists
	--
	insert into mapping.resource_uuid
	(
		scope_id,
		resource_type,
		unique_identifier,
		resource_uuid
	)
	values
	(
		_scope_id,
		_resource_type,
		_unique_identifier,
		uuid_generate_v4()
	)
	on conflict on constraint mapping_resourceuuid_scopeid_resourcetype_uniqueidentifier_pk
	do nothing;

	--------------------------------------------
	-- get uuid
	--
	select 
		resource_uuid into _resource_uuid
	from mapping.resource_uuid
	where scope_id = _scope_id
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
