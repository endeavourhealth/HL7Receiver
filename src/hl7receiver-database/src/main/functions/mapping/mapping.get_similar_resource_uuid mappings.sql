
create or replace function mapping.get_similar_resource_uuid_mappings
(
	_scope_name varchar(100),
	_unique_identifier_prefix varchar(200)
)
returns table
(
	scope_id char,
	resource_type varchar(100),
	unique_identifier varchar(200),
	resource_uuid uuid
)
as $$
declare
	_scope_id char(1);
begin
	
	--------------------------------------------
	-- lookup scope
	--
	_scope_name = trim(coalesce(_scope_name, ''));
	
	select
		s.scope_id into _scope_id
	from mapping.scope s
	where s.scope_name = _scope_name;
	
	if (_scope_id is null)
	then
		raise exception 'Could not find scope_name of %', _scope_name;
		return;
	end if;

	--------------------------------------------
	-- get resource uuid mappings
	--
	return query
	select 
		r.scope_id,
		r.resource_type,
		r.unique_identifier,
		r.resource_uuid
	from mapping.resource_uuid r
	where r.scope_id = _scope_id
	and r.unique_identifier like _unique_identifier_prefix || '%';
	
end;
$$ language plpgsql;
