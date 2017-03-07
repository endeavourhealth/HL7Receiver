
create or replace function helper.get_homerton_pids
(
	_cnn_number varchar
)
returns table
(
	message_id integer,
	message_type varchar,
	message_type_description varchar,
	message_date varchar,
	nhs_number varchar,
	cnn_number varchar,
	encounter_number varchar,
	attendance_number varchar
)
as $$
begin

	return query
	select 
		m.message_id,
		mt.message_type, 
		mt.description as message_type_description, 
		to_char(m.log_date, 'YYYY-MM-DD HH:mm') as message_date, 
		m.pid1 as nhs_number, 
		m.pid2 as cnn_number, 
		pid.f18 as encounter_number,
		pv1.f19 as attendance_number
	from log.message m
	inner join dictionary.message_type mt on m.inbound_message_type = mt.message_type
	inner join helper.get_split_segments(m.message_id) pid on m.message_id = pid.message_id and pid.segment_name = 'PID'
	left outer join helper.get_split_segments(m.message_id) pv1 on m.message_id = pv1.message_id and pv1.segment_name = 'PV1'
	where m.pid2 = _cnn_number
	order by m.message_id asc;

end;
$$ language plpgsql;
