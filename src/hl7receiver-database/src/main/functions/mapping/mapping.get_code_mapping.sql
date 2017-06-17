
create or replace function mapping.get_code_mapping
(
	_scope_name varchar(100),
	_source_code_context_name varchar(100),
	_source_code varchar(100),
	_source_code_system_identifier varchar(500),
	_source_term varchar(1000)
)
returns table
(
	code_id integer,
	target_code_action_id char(1),
	target_code varchar(100),
	target_code_system_identifier varchar(500),
	target_term varchar(100)
)
as $$
declare
	_scope_id char(1);
	_source_code_context_id integer;
	_source_code_is_case_insensitive boolean;
	_source_term_is_case_insensitive boolean;
	_code_action_id_unmapped_default char(1);
	_source_code_system_id integer;
	_code_id integer;
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
		return;
	end if;

	--------------------------------------------
	-- lookup source_code_context
	--
	_source_code_context_name = trim(coalesce(_source_code_context_name, ''));
	
	select
		code_context_id into _source_code_context_id
	from mapping.code_context 
	where code_context_name = _source_code_context_name;
	
	if (_source_code_context_id is null)
	then
		raise exception 'Could not find code_context_name of %', _source_code_context_name;
		return;
	end if;

	--------------------------------------------
	-- lookup source_code_system
	--
	select
		get_code_system_id into _source_code_system_id
	from mapping.get_code_system_id(_source_code_system_identifier);
	
	--------------------------------------------
	-- lookup mapping defaults
	--
	select
		source_code_is_case_insensitive,
		source_term_is_case_insensitive,
		code_action_id_unmapped_default
	into
		_source_code_is_case_insensitive,
		_source_term_is_case_insensitive,
		_code_action_id_unmapped_default
	from mapping.code_context
	where code_context_id = _source_code_context_id;
	
	--------------------------------------------
	-- clean code and term
	--
	_source_code = trim(coalesce(_source_code, ''));

	if (_source_code_is_case_insensitive)
	then
		_source_code = lower(_source_code);
	end if;

	_source_term = trim(coalesce(_source_term, ''));
	
	if (_source_term_is_case_insensitive)
	then
		_source_term = lower(_source_term);
	end if;
		
	--------------------------------------------
	-- if code and term are blank return 
	--
	if (_source_code = '' and _source_term = '')
	then
		return query
		select
			cast(null as integer) as code_id,
			_code_action_id_unmapped_default as target_code_action_id,
			cast(null as varchar) as target_code,
			cast(null as varchar) as target_code_system_identifier,
			cast(null as varchar) as target_term;
			
		return;
	end if;

	--------------------------------------------
	-- find mapping
	--
	select 
		c.code_id into _code_id
	from mapping.code c
	where c.source_code_context_id = _source_code_context_id
	and c.scope_id = _scope_id
	and c.source_code = _source_code
	and c.source_code_system_id = _source_code_system_id
	and c.source_term = _source_term;

	--------------------------------------------
	-- if no mapping, attempt to find global mapping
	--
	if (_code_id is null)
	then
		select 
			c.code_id into _code_id
		from mapping.code c
		where c.source_code_context_id = _source_code_context_id
		and c.scope_id = 'G'
		and c.source_code = _source_code
		and c.source_code_system_id = _source_code_system_id
		and c.source_term = _source_term;
	end if;
	
	--------------------------------------------
	-- if still no mapping, insert
	--
	if (_code_id is null)
	then
		insert into mapping.code
		(
			scope_id,
			source_code_context_id,
			source_code,
			source_code_system_id,
			source_term,
			is_mapped,
			target_code_action_id
		)
		values
		(
			_scope_id,
			_source_code_context_id,
			_source_code,
			_source_code_system_id,
			_source_term,
			false,
			_code_action_id_unmapped_default
		)
		returning mapping.code.code_id into _code_id;
	end if;

	--------------------------------------------
	-- return mapping
	--
	return query
	select
		c.code_id,
		c.target_code_action_id,
		c.target_code,
		s.code_system_identifier as target_code_system_identifier,
		c.target_term
	from mapping.code c
	left outer join mapping.code_system s on c.target_code_system_id = s.code_system_id
	where c.code_id = _code_id;
	
end;
$$ language plpgsql;

