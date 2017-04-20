
create or replace function log.get_channel_processor_lock
(
	_channel_id integer,
	_instance_id integer,
	_break_others_lock_seconds integer
)
returns boolean
as $$
declare
	_channel_claimed boolean;
	_current_instance_id integer;
	_last_heartbeat_date timestamp;
begin

	_channel_claimed = false;

	lock table log.channel_processor_lock in access exclusive mode;

	select
		instance_id, heartbeat_date into _current_instance_id, _last_heartbeat_date
	from log.channel_processor_lock
	where channel_id = _channel_id
	order by heartbeat_date desc;

	if (_current_instance_id is null)
	then
	
		insert into log.channel_processor_lock
		(
			channel_id,
			instance_id,
			heartbeat_date
		)
		values
		(
			_channel_id,
			_instance_id,
			now()
		);
		
		_channel_claimed = true;
		
	elseif (_current_instance_id = _instance_id)
	then
	
		update log.channel_processor_lock
		set heartbeat_date = now()
		where channel_id = _channel_id;
		
		_channel_claimed = true;
		
	elseif ((_current_instance_id != _instance_id) and (_last_heartbeat_date <= (now() - (_break_others_lock_seconds * interval '1 second'))))
	then
	
		update log.channel_processor_lock
		set
			instance_id = _instance_id,
			heartbeat_date = now()
		where channel_id = _channel_id;

		_channel_claimed = true;
		
	end if;

	return _channel_claimed;
	
end;
$$ language plpgsql;
