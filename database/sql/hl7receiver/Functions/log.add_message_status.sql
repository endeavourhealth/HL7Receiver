
create or replace function log.add_message_status
(
	_message_id integer,
	_instance_id integer,
	_message_status_type_id int,
	_message_status_content text,
	_in_error boolean,
	_error_message text
)
returns integer
as $$
declare
	_message_status_id integer;
begin

	insert into log.message_status
	(
		message_id,
		instance_id,
		message_status_date,
		message_status_type_id,
		is_success,
		error_message
	)
	values
	(
		_message_id,
		_instance_id,
		now(),
		_message_status_type_id,
		(not _in_error),
		_error_message
	)
	returning message_status_id into _message_status_id;
	
	if (trim(coalesce(_message_status_content, '')) != '')
	then
		insert into log.message_status_content
		(
			message_status_id,
			message_status_content
		)
		values
		(
			_message_status_id,
			_message_status_content
		);
	end if;
	
end;
$$ language plpgsql;

