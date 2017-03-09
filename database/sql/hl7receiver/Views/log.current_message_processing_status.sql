
create or replace view log.current_message_processing_status as

	with current as
	(
		select 
			s.message_id,
			s.attempt_id,
			row_number() over (partition by message_id order by attempt_id desc) as row_number
		from log.message_processing_status s
	)
	select 
		s1.*
	from current c
	inner join log.message_processing_status s1 on c.message_id = s1.message_id and c.attempt_id = s1.attempt_id
	where c.row_number = 1;
