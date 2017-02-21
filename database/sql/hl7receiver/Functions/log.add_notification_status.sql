
create or replace function log.add_notification_status
(
	_message_id integer,
	_was_success boolean,
	_instance_id integer,
	_request_message_uuid uuid,
	_request_message varchar,
	_response_message varchar,
	_exception_message varchar
)
returns void
as $$
declare
	_attempt_id integer;
begin

	insert into log.message_notification_status
	(
		message_id,
		attempt_id,
		was_success,
		instance_id,
		log_date,
		request_message_uuid,
		request_message,
		response_message,
		exception_message
	)
	select
		_message_id,
		coalesce(max(attempt_id), 0) + 1 as attempt_id,
		_was_success,
		_instance_id,
		now(),
		_request_message_uuid,
		_request_message,
		_response_message,
		_exception_message
	from log.message_notification_status
	where message_id = _message_id;
	
end;
$$ language plpgsql;

