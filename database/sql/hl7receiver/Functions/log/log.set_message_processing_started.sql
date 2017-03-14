
create or replace function log.set_message_processing_started
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
	_attempt_id integer;
	_next_attempt_date timestamp;
	_processing_started_status_id integer;
	_processing_started_date timestamp;
begin

	/*
		calculate current attempt id from previous attempt
	*/
	select
		(m.processing_attempt_id + 1) into _attempt_id
	from log.message m
	where m.message_id = _message_id;

	/*
		calculate the next attempt date
	*/
	select 
		now() + interval '1 second' * i.interval_seconds into _next_attempt_date
	from configuration.processing_attempt_interval i
	where i.processing_attempt_id = 
	(
		select least(max(processing_attempt_id), (_attempt_id + 1)) 
		from configuration.processing_attempt_interval
	);
	
	/*
		add a 'started' message processing status
	*/
	_processing_started_date = now();
	_processing_started_status_id = 1;
	
	update log.message
	set
		message_status_id = _processing_started_status_id,
		message_status_date = _processing_started_date,
		is_complete = false,
		processing_attempt_id = _attempt_id,
		next_attempt_date = _next_attempt_date
	where message_id = _message_id;
		
	insert into log.message_status_history
	(
		message_id,
		processing_attempt_id,
		message_status_id,
		message_status_date,
		is_complete,
		error_message,
		instance_id
	)
	values
	(
		_message_id,
		_attempt_id,
		_processing_started_status_id,
		_processing_started_date,
		false,
		null,
		_instance_id
	);

	/*
		delete any previous attempt's message processing content
	*/
	if exists
	(
		select *
		from configuration.get_channel_option_by_message_id(_message_id, 'KeepOnlyCurrentMessageProcessingContentAttempt')
		where get_channel_option_by_message_id = 'TRUE'
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

