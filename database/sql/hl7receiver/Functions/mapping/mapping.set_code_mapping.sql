
create or replace function mapping.set_code_mapping
(
	_source_code_origin_name varchar(100),
	_source_code_context_name varchar(100),
	_source_code varchar(100),
	_source_code_system_identifier varchar(500),
	_source_term varchar(500),
	_target_code varchar(100),
	_target_code_system_identifier varchar(500),
	_target_term varchar(500)
)
returns void
as $$
declare
	_target_code_system_id integer;
	_code_id integer;
begin

	--------------------------------------------
	-- lookup target source_code_system
	--
	select
		get_code_system_id into _target_code_system_id
	from mapping.get_code_system_id(_target_code_system_identifier);

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
	perform
	from mapping.set_code_mapping_internal
	(
		_code_id := _code_id,
		_target_code := _target_code,
		_target_code_system_identifier := _target_code_system_identifier,
		_target_term := _target_term
	);

end;
$$ language plpgsql;

