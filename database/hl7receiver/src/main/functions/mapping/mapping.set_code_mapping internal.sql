
create or replace function mapping.set_code_mapping_internal
(
	_code_id integer,
	_target_code varchar(100),
	_target_code_system_identifier varchar(500),
	_target_term varchar(500)
)
returns void
as $$
declare
	_target_code_system_id integer;
begin

	--------------------------------------------
	-- clean parameters
	--
	_target_code = trim(coalesce(_target_code, ''));
	_target_term = trim(coalesce(_target_term, ''));

	--------------------------------------------
	-- lookup target source_code_system
	--
	select
		get_code_system_id into _target_code_system_id
	from mapping.get_code_system_id(_target_code_system_identifier);

	--------------------------------------------
	-- ensure we have a code
	--
	if not exists
	(
		select *
		from mapping.code
		where code_id = _code_id	
	)
	then
		raise exception 'Could not find (or create) code';
		return;
	end if;	
	
	--------------------------------------------
	-- set the mapping
	--
	update mapping.code
	set
		is_mapped = true,
		target_code_action_id = 'T',
		target_code = _target_code,
		target_code_system_id = _target_code_system_id,
		target_term = _target_term
	where code_id = _code_id;

end;
$$ language plpgsql;

