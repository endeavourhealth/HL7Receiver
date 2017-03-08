
create or replace function mapping.get_code
(
	_code_set_name varchar(100),
	_code_context_name varchar(100),
	_original_code varchar(100),
	_original_system varchar(100),
	_original_term varchar(1000)
)
returns table
(
	is_mapped boolean,
	mapped_code varchar(100),
	mapped_system varchar(100),
	mapped_term varchar(100)
)
as $$
declare
	_code_set_id integer;
	_code_context_id integer;
begin

	select
		code_set_id into _code_set_id
	from mapping.code_set 
	where code_set_name = _code_set_name;
	
	if (_code_set_id is null)
	then
		raise exception 'Could not find code_set_name of %', _code_set_name;
		return;
	end if;
	
	select
		code_context_id into _code_context_id
	from mapping.code_context
	where code_context_name = _code_context_name;

	if (_code_context_id is null)
	then
		raise exception 'Could not find code_context_name of %', _code_context_name;
		return;
	end if;
	
	if not exists
	(
		select *
		from mapping.code
		where code_set_id = _code_set_id
		and code_context_id = _code_context_id
		and original_code = _original_code
		and original_system = _original_system
		and original_term = _original_term
	)
	then
		insert into mapping.code
		(
			code_set_id,
			code_context_id,
			original_code,
			original_system,
			original_term
		)
		values
		(
			_code_set_id,
			_code_context_id,
			_original_code,
			_original_system,
			_original_term
		);	
	end if;
	
	return query
	select
		c.mapped_code,
		c.mapped_system,
		c.mapped_term
	from mapping.code c
	where c.code_set_id = _code_set_id
	and c.code_context_id = _code_context_id
	and c.original_code = _original_code
	and c.original_system = _original_system
	and c.original_term = _original_term
	and c.is_mapped;
		
end;
$$ language plpgsql;

