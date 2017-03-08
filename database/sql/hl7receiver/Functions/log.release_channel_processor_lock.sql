
create or replace function log.release_channel_processor_lock
(
	_channel_id integer,
	_instance_id integer
)
returns void
as $$
begin

	lock table log.release_channel_processor_lock in access exclusive mode;

	delete from log.release_channel_processor_lock
	where channel_id = _channel_id
	and instance_id = _instance_id;
	
end;
$$ language plpgsql;
