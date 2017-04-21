
create or replace function log.open_connection
(
	_instance_id integer,
	_channel_id integer,
	_local_port integer,
	_remote_host varchar(100)	,
	_remote_port integer
)
returns table
(
	connection_id integer
)
as $$
declare
	_connection_id integer;
begin

	insert into log.connection
	(
		instance_id,
		channel_id,
		local_port,
		remote_host,
		remote_port,
		connect_date
	)
	values
	(
		_instance_id,
		_channel_id,
		_local_port,
		_remote_host,
		_remote_port,
		now()
	)
	returning connection.connection_id into _connection_id;
	
	return query 
	select 
		_connection_id as connection_id;
	
end;
$$ language plpgsql;
