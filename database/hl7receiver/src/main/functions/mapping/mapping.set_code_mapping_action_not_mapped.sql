
create or replace function mapping.set_code_mapping_action_not_mapped
(
	_source_code_origin_name varchar(100),
	_source_code_context_name varchar(100),
	_source_code varchar(100),
	_source_code_system_identifier varchar(500),
	_source_term varchar(500),
	_target_code_action_id char(1)
)
returns void
as $$
declare
	_code_id integer;
begin

	--------------------------------------------
	-- lookup target code_action
	--
	if not exists
	(
		select *
		from mapping.code_action
		where code_action_id = _target_code_action_id
		and (not is_mapped)
	)
	then
		raise exception 'code action does not exist or is a mapped action';
		return; 
	end if;

	--------------------------------------------
	-- find the code
	--
	select 
		code_id into _code_id
	from mapping.get_code_mapping
	(
		_source_code_origin_name := _source_code_origin_name,
		_source_code_context_name := _source_code_context_name,
		_source_code := _source_code,
		_source_code_system_identifier := _source_code_system_identifier,
		_source_term := _source_term
	);

	--------------------------------------------
	-- set the mapping
	--
	update mapping.code 
	set 
		is_mapped = false, 
		target_code_action_id = 'X', 
		target_code = null, 
		target_code_system_id = null, 
		target_term = null
	where code_id = _code_id;

end;
$$ language plpgsql;

