
create or replace function helper.get_segments
(
	_message_id integer,
	_max_message_id integer default null
)
returns table
(
	message_id integer,
	segment_name text,
	segment_text text
)
as $$
begin

	return query
	select 
		s.message_id,
		split_part(s.segment_text, '|', 1) as segment_name,
		s.segment_text as segment_text
	from
	(
		select 
			t.message_id,
			generate_subscripts(t.segment_text, 1) as segment_index,
			unnest(t.segment_text) as segment_text
		from
		(
			select 
				m.message_id,
				string_to_array(m.inbound_payload, chr(13)) segment_text
			from log.message m
			where (_max_message_id is null and m.message_id = _message_id) 
			or (m.message_id >= _message_id and m.message_id <= _max_message_id)
		) t
	) s
	where s.segment_text != ''
	order by s.message_id, s.segment_index;

end;
$$ language plpgsql;
