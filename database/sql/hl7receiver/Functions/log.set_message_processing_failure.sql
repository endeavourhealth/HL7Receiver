
create or replace function log.set_message_processing_failure
(
	_message_id integer,
	_attempt_id integer,
	_processing_status_id integer,
	_error_message varchar
)
returns void
as $$
declare
	_is_complete boolean;
	_row_count integer;
begin

	select
		is_complete into _is_complete
	from log.message_processing_status
	where message_id = _message_id
	and attempt_id = _attempt_id;
	
	if (_is_complete)
	then
		raise exception 'Cannot update message processing status for message % attempt % because this attempt is already complete', _message_id, _attempt_id;
		return;
	end if;

	with updated_rows as
	(
		update log.message_processing_status
		set
			processing_status_id = _processing_status_id,
			is_complete = false,
			error_message = _error_message
		where message_id = _message_id
		and attempt_id = _attempt_id
		returning message_id
	)
	select
		count(*) into _row_count
	from updated_rows;
	
	if (_row_count != 1)
	then
		raise exception 'Error updating message processing status for message %', _message_id;
		return;
	end if;
		
end;
$$ language plpgsql;

