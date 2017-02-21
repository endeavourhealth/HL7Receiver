
create or replace function log.log_error_digest
(
	_log_class varchar(1000),
	_log_method varchar(1000),
	_log_message varchar(1000),
	_exception varchar(1000)
)
returns integer
as $$

	insert into log.error_digest
	(
		error_digest_id,
		error_count,
		last_log_date,
		log_class,
		log_method,
		log_message,
		exception
	)
	select
		coalesce(max(error_digest_id), 0) + 1,
		1,
		now(),
		_log_class,
		_log_method,
		_log_message,
		_exception
	from log.error_digest
	on conflict (log_class, log_method, log_message, exception) do 
	update
	set 
		error_count = error_digest.error_count + 1,
		last_log_date = now()
	where error_digest.log_class = excluded.log_class
	and error_digest.log_method = excluded.log_method
	and error_digest.log_message = excluded.log_message
	and error_digest.exception = excluded.exception	returning error_digest_id;
	
$$ language sql;
