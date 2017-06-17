
create or replace function log.reprocess_failed_messages
(
	_channel_id integer,
	_instance_id integer
)
returns table
(
	message_count bigint
)
as $$
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
	with updated_rows as
	(
		update log.message 
		set next_attempt_date = case when next_attempt_date > now() then now() else next_attempt_date end
		where message_id in
		(
			select 
				m.message_id 
			from log.message m
			inner join dictionary.message_status s on m.message_status_id = s.message_status_id
			where m.channel_id = _channel_id
			and m.next_attempt_date is not null 
			and (not s.is_complete)
	
		)
		returning message_id
	)
	select 
		count(*) as message_count 
	from updated_rows;
		
end;
$$ language plpgsql;

