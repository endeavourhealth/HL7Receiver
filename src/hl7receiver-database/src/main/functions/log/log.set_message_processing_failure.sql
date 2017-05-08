
create or replace function log.set_message_processing_failure
(
	_message_id integer,
	_attempt_id integer,
	_message_status_id integer,
	_error_message varchar,
	_instance_id integer
)
returns void
as $$
declare
	_message_status_date timestamp;
begin

	if exists
	(
		select
			*
		from log.message m
		inner join dictionary.message_status s on m.message_status_id = s.message_status_id
		where m.message_id = _message_id
		and m.processing_attempt_id = _attempt_id
		and s.is_complete
	)
	then
		raise exception 'Cannot update message processing status for message % attempt % because this attempt is already complete', _message_id, _attempt_id;
		return;
	end if;

	_message_status_date = now();
	
	update log.message
	set
		message_status_id = _message_status_id,
		message_status_date = _message_status_date,
		is_complete = false,
		error_message = _error_message,
		processing_attempt_id = _attempt_id
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
		_message_status_id,
		_message_status_date,
		false,
		_error_message,
		_instance_id
	);
	
end;
$$ language plpgsql;

