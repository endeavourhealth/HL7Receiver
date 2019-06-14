
create or replace function log.set_message_processing_success
(
	_message_id integer,
	_attempt_id integer,
	_instance_id integer
)
returns void
as $$
declare
	_message_status_date timestamp;
begin

	_message_status_date = now();
	
	update log.message
	set
		message_status_id = 9,
		message_status_date = _message_status_date,
		is_complete = true,
		error_message = null,
		processing_attempt_id = _attempt_id,
		next_attempt_date = null
	where message_id = _message_id;
	
	insert into log.message_status_history
	(
		message_id,
		processing_attempt_id,
		message_status_id,
		message_status_date,
		is_complete,
		error_message,
		instance_id
	)
	values
	(
		_message_id,
		_attempt_id,
		9,
		_message_status_date,
		true,
		null,
		_instance_id
	);
		
end;
$$ language plpgsql;

