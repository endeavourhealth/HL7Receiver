
create or replace function log.get_next_unnotified_message
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
	request_message_uuid uuid
)
as $$
begin

	if not exists
	(
		select * 
		from log.channel_forwarder_lock
		where channel_id = _channel_id
		and instance_id = _instance_id
	)
	then
		raise exception 'instance_id % does not have channel forwarder lock', _instance_id;
		return;
	end if;
	
	return query
	select 
		m.message_id,
		m.message_control_id,
		m.message_sequence_number,
		m.message_date,
		m.inbound_message_type,
		m.inbound_payload,
		coalesce(s2.request_message_uuid, uuid_generate_v4()) as request_message_uuid
	from log.message m
	left outer join log.message_notification_status s1 on m.message_id = s1.message_id and s1.was_success = true
	left outer join log.message_notification_status s2 on m.message_id = s2.message_id and s2.attempt_id = 1
	where m.channel_id = _channel_id
	and s1.message_id is null
	order by m.message_date asc, m.log_date asc
	limit 1;
	
end;
$$ language plpgsql;
