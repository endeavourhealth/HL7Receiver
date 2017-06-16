
create or replace function helper.move_message_to_dead_letter
(
	_message_id integer,
	_reason varchar(500)
)
returns void
as $$
begin

	insert into log.dead_letter
	(
		log_date,
		instance_id,
		channel_id,
		connection_id,
		local_host,
		local_port,
		remote_host,
		remote_port,
		sending_application,
		sending_facility,
		receiving_application,
		receiving_facility,
		message_control_id,
		message_sequence_number,
		message_date,
		pid1,
		pid2,
		inbound_message_type,
		inbound_payload,
		outbound_message_type,
		outbound_payload,
		exception,
		dead_letter_uuid,
		outbound_ack_code
	)
	select 
		m.log_date,
		c.instance_id,
		m.channel_id,
		m.connection_id,
		i.hostname,
		c.local_port,
		c.remote_host,
		c.remote_port,
		cc.sending_application,
		cc.sending_facility,
		cc.receiving_application,
		cc.receiving_facility,
		m.message_control_id,
		m.message_sequence_number,
		m.message_date,
		m.pid1,
		m.pid2,
		m.inbound_message_type,
		m.inbound_payload,
		m.outbound_message_type,
		m.outbound_payload,
		'Message manually moved from log.message, old message_id = ' || cast(m.message_id as varchar(10)) || ', reason = ' || coalesce(_reason, ''),
		m.message_uuid,
		'AA'
	from log.message m
	inner join log.connection c on m.connection_id = c.connection_id
	inner join configuration.channel cc on m.channel_id = cc.channel_id
	inner join log.instance i on c.instance_id = i.instance_id
	where m.message_id = _message_id;

	delete from log.message_processing_content
	where message_id = _message_id;
	
	delete from log.message_status_history
	where message_id = _message_id;
	
	delete from log.message_queue
	where message_id = _message_id;
	
	delete from log.message
	where message_id = _message_id;

end;
$$ language plpgsql;
