
create or replace function mapping.get_code
(
	_source_code_origin_name varchar(100),
	_source_code_context_name varchar(100),
	_source_code varchar(100),
	_source_code_system_identifier varchar(500),
	_source_term varchar(1000)
)
returns table
(
	is_mapped boolean,
	target_code_action_id char(1),
	target_code varchar(100),
	target_code_system_identifier varchar(500),
	target_term varchar(100)
)
as $$
declare
	_source_code_origin_id char(1);
	_source_code_context_id integer;
	_code_action_id_unmapped_default char(1);
	_source_code_system_id integer;
	_code_id integer;
begin

	select
		code_origin_id into _source_code_origin_id
	from mapping.code_origin
	where code_origin_name = _source_code_origin_name;

	if (_source_code_origin_id is null)
	then
		raise exception 'Could not find code_origin_name of %', _source_code_origin_name;
		return;
	end if;

	select
		code_context_id, 
		code_action_id_unmapped_default 
	into
		_source_code_context_id, 
		_code_action_id_unmapped_default
	from mapping.code_context
	where code_context_name = _source_code_context_name;
	
	if (_source_code_context_id is null)
	then
		raise exception 'Could not find code_context_name of %', _source_code_context_name;
		return;
	end if;

	if (coalesce(_source_code_system_identifier, '') = '')
	then
		_source_code_system_identifier = 'NO-CODE-SYSTEM';
	end if;
	
	select
		code_system_id into _source_code_system_id
	from mapping.code_system 
	where code_system_identifier = _source_code_system_identifier;
	
	if (_source_code_system_id is null)
	then
		raise exception 'Could not find code_system_identifier of %', _source_code_system_identifier;
		return;
	end if;

	select 
		c.code_id into _code_id
	from mapping.code c
	where c.source_code_context_id = _source_code_context_id
	and c.source_code_origin_id = _source_code_origin_id
	and c.source_code = _source_code
	and c.source_code_system_id = _source_code_system_id
	and c.source_term = _source_term;
	
	if (_code_id is null)
	then
		insert into mapping.code
		(
			source_code_origin_id,
			source_code_context_id,
			source_code,
			source_code_system_id,
			source_term,
			is_mapped,
			target_code_action_id
		)
		values
		(
			_source_code_origin_id,
			_source_code_context_id,
			_source_code,
			_source_code_system_id,
			_source_term,
			false,
			_code_action_id_unmapped_default
		)
		returning code_id into _code_id;
	end if;
	
	return query
	select
		c.is_mapped,
		c.target_code_action_id,
		c.target_code,
		s.code_system_identifier as target_code_system_identifier,
		c.target_term
	from mapping.code c
	left outer join mapping.code_system s on c.target_code_system_id = s.code_system_id
	where c.code_id = _code_id;
	
end;
$$ language plpgsql;

