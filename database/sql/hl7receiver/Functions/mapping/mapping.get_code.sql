
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
	target_code_action_id char(1),
	target_code varchar(100),
	target_code_system_identifier varchar(500),
	target_term varchar(100)
)
as $$
declare
	_source_code_origin_id char(1);
	_source_code_context_id integer;
	_source_code_is_case_insensitive boolean;
	_code_action_id_unmapped_default char(1);
	_source_code_system_id integer;
	_code_id integer;
begin

	--------------------------------------------
	-- lookup source_code_origin
	--
	select
		code_origin_id into _source_code_origin_id
	from mapping.code_origin
	where code_origin_name = _source_code_origin_name;

	if (_source_code_origin_id is null)
	then
		raise exception 'Could not find code_origin_name of %', _source_code_origin_name;
		return;
	end if;

	--------------------------------------------
	-- lookup source_code_context
	--
	select
		code_context_id, 
		source_code_is_case_insensitive,
		code_action_id_unmapped_default
	into
		_source_code_context_id, 
		_source_code_is_case_insensitive,
		_code_action_id_unmapped_default
	from mapping.code_context
	where code_context_name = _source_code_context_name;
	
	if (_source_code_context_id is null)
	then
		raise exception 'Could not find code_context_name of %', _source_code_context_name;
		return;
	end if;

	--------------------------------------------
	-- clean code, term and system
	--
	_source_code = trim(coalesce(_source_code, ''));
	_source_term = trim(coalesce(_source_term, ''));
	_source_code_system_identifier = trim(coalesce(_source_code_system_identifier, ''));

	if (_source_code_is_case_insensitive)
	then
		_source_code = lower(_source_code);
	end if;
	
	--------------------------------------------
	-- if code and term and blank return 
	--
	if (_source_code = '' and _source_term = '')
	then
		return query
		select
			_code_action_id_unmapped_default as target_code_action_id,
			null as target_code,
			null as target_code_system_identifier,
			null as target_term;
	end if;
	
	--------------------------------------------
	-- lookup source_code_system
	--
	if (_source_code_system_identifier = '')
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

	--------------------------------------------
	-- find mapping
	--
	select 
		c.code_id into _code_id
	from mapping.code c
	where c.source_code_context_id = _source_code_context_id
	and c.source_code_origin_id = _source_code_origin_id
	and c.source_code = _source_code
	and c.source_code_system_id = _source_code_system_id
	and c.source_term = _source_term;
	
	--------------------------------------------
	-- if no mapping, insert
	--
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

	--------------------------------------------
	-- return mapping
	--
	return query
	select
		c.target_code_action_id,
		c.target_code,
		s.code_system_identifier as target_code_system_identifier,
		c.target_term
	from mapping.code c
	left outer join mapping.code_system s on c.target_code_system_id = s.code_system_id
	where c.code_id = _code_id;
	
end;
$$ language plpgsql;

