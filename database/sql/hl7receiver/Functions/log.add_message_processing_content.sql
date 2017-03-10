
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
		attempt_id,
		processing_content_type_id,
		content
	)
	values
	(
		_message_id,
		_attempt_id,
		_processing_content_type_id,
		_content
	);
	
--	-- remove previous attempt's content for this processing_content_type_id
--	delete from log.message_processing_content
--	where message_id = _message_id
--	and processing_content_type_id = _processing_content_type_id
--	and attempt_id < _attempt_id;
		
end;
$$ language plpgsql;

