
create or replace function log.add_message_processing_content
(
	_message_id integer,
	_attempt_id integer,
	_processing_content_type_id integer,
	_content varchar
)
returns void
as $$
begin

	insert into log.message_processing_content
	(
		message_id,
		processing_attempt_id,
		content_saved_date,
		processing_content_type_id,
		content
	)
	values
	(
		_message_id,
		_attempt_id,
		now(),
		_processing_content_type_id,
		_content
	);
				
end;
$$ language plpgsql;
