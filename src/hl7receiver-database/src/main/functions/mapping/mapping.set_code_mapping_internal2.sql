-- FUNCTION: mapping.set_code_mapping_internal(integer, character varying, character varying, character varying)

-- DROP FUNCTION mapping.set_code_mapping_internal(integer, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION mapping.set_code_mapping_internal(
	_code_id integer,
	_target_code character varying,
	_target_code_system_id integer,
	_target_term character varying)
    RETURNS void
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE
AS $BODY$

begin

	--------------------------------------------
	-- clean parameters
	--
	_target_code = trim(coalesce(_target_code, ''));
	_target_term = trim(coalesce(_target_term, ''));

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

$BODY$;

ALTER FUNCTION mapping.set_code_mapping_internal(integer, character varying, integer, character varying)
    OWNER TO postgres;

GRANT EXECUTE ON FUNCTION mapping.set_code_mapping_internal(integer, character varying, integer, character varying) TO discover;

GRANT EXECUTE ON FUNCTION mapping.set_code_mapping_internal(integer, character varying, integer, character varying) TO PUBLIC;

GRANT EXECUTE ON FUNCTION mapping.set_code_mapping_internal(integer, character varying, integer, character varying) TO postgres;

