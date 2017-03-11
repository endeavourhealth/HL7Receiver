
create or replace function configuration.get_channel_option_by_message_id
(
	_message_id integer,
	_channel_option_type varchar(100)
)
returns varchar(100)
as $$
declare
	_channel_id integer;
	_channel_option_value varchar(100);
begin

	if not exists
	(
		select *
		from log.message m
		where m.message_id = _message_id
	)
	then
		raise exception 'message id % does not exist', _message_id;
		return null;
	end if;

	select m.channel_id into _channel_id
	from log.message m
	where m.message_id = _message_id;

	select
		configuration.get_channel_option(_channel_id, _channel_option_type) into _channel_option_value;

	return _channel_option_value;
	
end;
$$ language plpgsql;
