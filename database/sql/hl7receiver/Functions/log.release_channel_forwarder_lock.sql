
create or replace function log.release_channel_forwarder_lock
(
	_channel_id integer,
	_instance_id integer
)
returns void
as $$
begin

	lock table log.channel_forwarder_lock in access exclusive mode;

	delete from log.channel_forwarder_lock
	where channel_id = _channel_id
	and instance_id = _instance_id;
	
end;
$$ language plpgsql;
