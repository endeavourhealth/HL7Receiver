
create or replace function log.set_message_processing_success
(
	_message_id integer,
	_attempt_id integer
)
returns void
as $$
declare
	_row_count integer;
begin

	with updated_rows as
	(
		update log.message_processing_status
		set
			processing_status_id = 9,
			is_complete = true,
			error_message = null,
			next_attempt_date = null
		where message_id = _message_id
		and attempt_id = _attempt_id
		returning message_id
	)
	select
		count(*) into _row_count
	from updated_rows;
	
	if (_row_count != 1)
	then
		raise exception 'Error completing message processing for message %', _message_id;
		return;
	end if;
		
end;
$$ language plpgsql;

