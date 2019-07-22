-- FUNCTION: log.log_message(integer, integer, character varying, character varying, timestamp without time zone, character varying, character varying, character varying, text, character varying, text)

-- DROP FUNCTION log.log_message(integer, integer, character varying, character varying, timestamp without time zone, character varying, character varying, character varying, text, character varying, text);

CREATE OR REPLACE FUNCTION log.log_message(
	_channel_id integer,
	_connection_id integer,
	_message_control_id character varying,
	_message_sequence_number character varying,
	_message_date timestamp without time zone,
	_pid1 character varying,
	_pid2 character varying,
	_inbound_message_type character varying,
	_inbound_payload text,
	_outbound_message_type character varying,
	_outbound_payload text)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE
AS $BODY$
declare
	_message_id integer;
	_instance_id integer;
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
		message_uuid,
		message_status_id,
		message_status_date,
		is_complete,
		error_message,
		processing_attempt_id,
		next_attempt_date
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
		uuid_generate_v4(),
		0,
		_log_date,
		false,
		null,
		0,
		null
	)
	returning message_id into _message_id;

	select
		c.instance_id into _instance_id
	from log.connection c
	where c.connection_id = _connection_id;

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
		0,
		0,
		_log_date,
		false,
		null,
		_instance_id
	);

	INSERT INTO log.last_message (
		message_id,
		channel_id,
		log_date
	) VALUES (
		_message_id,
		_channel_id,
		_log_date
	) ON CONFLICT (channel_id) DO UPDATE SET
		message_id = EXCLUDED.message_id,
		log_date = EXCLUDED.log_date;

	return _message_id;

end;
$BODY$;
