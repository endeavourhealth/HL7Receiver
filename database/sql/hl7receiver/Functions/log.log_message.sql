
create or replace function log.log_message
(
	_channel_id integer,
	_connection_id integer,
	_message_control_id varchar(100),
	_message_sequence_number varchar(100),
	_message_date timestamp,
	_pid1 varchar(100),
	_pid2 varchar(100),
	_inbound_message_type varchar(100),
	_inbound_payload text,
	_outbound_message_type varchar(100),
	_outbound_payload text
)
returns integer
as $$
declare
	_message_id integer;
	_log_date timestamp;
begin

	_log_date = now();

	insert into log.message
	(
		channel_id,
		connection_id,
		log_date,
		message_control_id,
		message_sequence_number,
		message_date,
		pid1,
		pid2,
		inbound_message_type,
		inbound_payload,
		outbound_message_type,
		outbound_payload,
		message_uuid
	)
	values
	(
		_channel_id,
		_connection_id,
		_log_date,
		_message_control_id,
		_message_sequence_number,
		_message_date,
		_pid1,
		_pid2,
		_inbound_message_type,
		_inbound_payload,
		_outbound_message_type,
		_outbound_payload,
		uuid_generate_v4()
	)
	returning message_id into _message_id;
	
	perform log.add_message_status
	(
		_message_id := _message_id,
		_instance_id := _instance_id,
		_message_status_type_id := 1, -- message received
		_message_status_content := null,
		_in_error := false,
		_error_message := null
	);
	
	return _message_id;
	
end;
$$ language plpgsql;
