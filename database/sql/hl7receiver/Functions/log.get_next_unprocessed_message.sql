
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
	
	select
		m.message_id into _message_id
	from log.message m
	left outer join log.message_status ms on m.message_id = ms.message_id and ms.message_status_type_id = 4  -- notification success
	where m.channel_id = _channel_id
	and ms.message_id is null
	order by m.message_date asc, m.log_date asc
	limit 1;
	
	perform log.add_message_status
	(
		_message_id := _message_id,
		_instance_id := _instance_id,
		_message_status_type_id := 2, -- retrieved for processing
		_message_status_content := null,
		_in_error := false,
		_error_message := null
	);
	
	return query
	select 
		m.message_id,
		m.message_control_id,
		m.message_sequence_number,
		m.message_date,
		m.inbound_message_type,
		m.inbound_payload,
		m.message_uuid
	from log.message m
	where m.message_id = _message_id;
	
end;
$$ language plpgsql;
