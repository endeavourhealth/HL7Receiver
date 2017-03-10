
create or replace function log.start_message_processing
(
	_message_id integer,
	_instance_id integer
)
returns table
(
	attempt_id integer
)
as $$
declare
	_previous_attempt_id integer;
	_attempt_id integer;
	_next_attempt_date timestamp;
begin

	/*
		calculate current attempt id from previous attempt
	*/
	select
		s.attempt_id into _previous_attempt_id
	from log.current_message_processing_status s
	where s.message_id = _message_id;
	
	_attempt_id = coalesce(_previous_attempt_id, 0) + 1;
	
	/*
		determine if there are any remaining attempts allowed
	*/
	if not exists
	(
		select *
		from configuration.processing_attempt_interval i		where i.attempt_id = _attempt_id
	)
	then
		raise exception 'No more attempts to process message % left', _message_id;
		return;
	end if;
	
	/*
		calculate the next attempt date
	*/
	select 
		now() + interval '1 second' * i.interval_seconds into _next_attempt_date
	from configuration.processing_attempt_interval i
	where i.attempt_id = (_attempt_id + 1);
	
	/*
		add a 'started' message processing status
	*/
	insert into log.message_processing_status
	(
		message_id,
		attempt_id,
		attempt_date,
		processing_status_id,
		is_complete,
		error_message,
		next_attempt_date,
		processing_instance_id
	)
	values
	(
		_message_id,
		_attempt_id,
		now(),
		1,
		false,
		'',
		_next_attempt_date,
		_instance_id
	);

	/*
		delete any previous attempt's message processing content
	*/
	if exists
	(
		select * 
		from configuration.channel_option co
		inner join log.message m on co.channel_id = m.channel_id
		where m.message_id = _message_id
		and co.channel_option_type = 'KeepOnlyCurrentMessageProcessingContentAttempt'
		and co.channel_option_value = 'TRUE'
	)
	then
		delete from log.message_processing_content mpc
		where mpc.message_id = _message_id
		and mpc.attempt_id < _attempt_id;	
	end if;
	
	/*
		return the attempt id
	*/
	return query
	select
		_attempt_id as attempt_id;
		
end;
$$ language plpgsql;

