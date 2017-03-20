
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
			mq.message_id
		from log.message_queue mq
		where mq.channel_id = _channel_id
		order by
			mq.message_date asc,
			mq.log_date asc
		limit (select cast(configuration.get_channel_option(_channel_id, 'MaxSkippableProcessingErroredMessages') as integer) + 1)
	)
	select
		m.message_id,
		m.message_control_id,
		m.message_sequence_number,
		m.message_date,
		m.inbound_message_type,
		m.inbound_payload,
		m.message_uuid
	from candidates c
	inner join log.message m on c.message_id = m.message_id
	where (coalesce(m.next_attempt_date, now()) <= now())
	order by 
		m.message_date asc, 
		m.log_date asc
	limit 1;
	
end;
$$ language plpgsql;
