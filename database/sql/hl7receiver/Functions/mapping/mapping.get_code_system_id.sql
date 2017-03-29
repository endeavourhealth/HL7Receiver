
create or replace function mapping.get_code_system_id
(
	_code_system_identifier varchar(500)
)
returns integer
as $$
declare
	_code_system_id integer;
begin

	if (trim(coalesce(_code_system_identifier, '')) = '')
	then
		_code_system_identifier = 'NO-CODE-SYSTEM';
	end if;
	
	select
		code_system_id into _code_system_id
	from mapping.code_system 
	where code_system_identifier = _code_system_identifier;
	
	if (_code_system_id is null)
	then
		raise exception 'Could not find code_system_identifier of %', _code_system_identifier;
		return null;
	end if;
	
	return _code_system_id;
		
end;
$$ language plpgsql;
