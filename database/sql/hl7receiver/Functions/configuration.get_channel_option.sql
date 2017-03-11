
create or replace function configuration.get_channel_option
(
	_channel_id integer,
	_channel_option_type varchar(100)
)
returns varchar(100)
as $$
declare
	_channel_option_value varchar(100);
begin

	if not exists
	(
		select *
		from dictionary.channel_option_type
		where channel_option_type = _channel_option_type
	)
	then
		raise exception 'channel option type % does not exists', _channel_option_type;
		return null;
	end if;

	select 
		coalesce(channel_option_value, default_value) as option_value into _channel_option_value
	from dictionary.channel_option_type ot
	left outer join configuration.channel_option co on ot.channel_option_type = co.channel_option_type and co.channel_id = _channel_id
	where ot.channel_option_type = _channel_option_type;

	return _channel_option_value;
	
end;
$$ language plpgsql;
