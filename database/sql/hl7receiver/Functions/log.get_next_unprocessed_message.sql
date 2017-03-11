
create or replace function log.get_next_unprocessed_message
(
	_channel_id integer,
	_instance_id integer
)
returns table
(
	message_id integer,
	message_control_id varchar,
	message_sequence_number varchar,
	message_date timestamp,
	inbound_message_type varchar,
	inbound_payload varchar,
	message_uuid uuid
)
as $$
declare
	_message_id integer;
begin

	if not exists
	(
		select * 
		from log.channel_processor_lock
		where channel_id = _channel_id
		and instance_id = _instance_id
	)
	then
		raise exception 'instance_id % does not have channel processor lock', _instance_id;
		return;
	end if;
	
	return query
	with candidates as
	(
		select
			m.message_id,
			m.message_control_id,
			m.message_sequence_number,
			m.message_date,
			m.log_date,
			m.inbound_message_type,
			m.inbound_payload,
			m.message_uuid,
			coalesce(s.next_attempt_date, now()) as next_attempt_date
		from log.message m
		left outer join log.current_message_processing_status s on m.message_id = s.message_id
		where m.channel_id = _channel_id
		and not coalesce(s.is_complete, false)
		order by 
			m.message_date asc, 
			m.log_date asc
		limit (select cast(configuration.get_channel_option(_channel_id, 'MaxSkippableProcessingErroredMessages') as integer) + 1)
	)
	select
		c.message_id,
		c.message_control_id,
		c.message_sequence_number,
		c.message_date,
		c.inbound_message_type,
		c.inbound_payload,
		c.message_uuid
	from candidates c
	where (c.next_attempt_date <= now())
	order by 
		c.message_date asc, 
		c.log_date asc
	limit 1;

end;
$$ language plpgsql;
