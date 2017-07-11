
create or replace function helper.get_homerton_pvs
(
	_cnn_number varchar
)
returns table
(
	message_id integer,
	message_type varchar,
	message_type_description varchar,
	message_date text,

	-- core columns
	pv1_2_patient_class text,
	pv1_3_patient_location text,
	pv1_7_attending_doctor text,
	pv1_10_hospital_service text,
	pv1_18_patient_type text,
	pv1_19_attendance_number text,
	pv1_41_account_status text,
	pv1_44_admit_date text,
	pv1_45_discharge_date text,
	pv2_8_expected_admit_date text,
	zvi_15_assign_to_location_date text,

	
	-- all pv1
	message_ida integer, segment_namea text, f1a text, f2a text, f3a text, f4a text, f5a text, f6a text, f7a text, f8a text, f9a text, f10a text, f11a text, f12a text, f13a text, f14a text, f15a text, f16a text, f17a text, f18a text, 
	f19a text, f20a text, f21a text, f22a text, f23a text, f24a text, f25a text, f26a text, f27a text, f28a text, f29a text, f30a text, f31a text, f32a text, f33a text, f34a text, f35a text, f36a text, f37a text, f38a text, f39a text, 
	f40a text, f41a text, f42a text, f43a text, f44a text, f45a text, f46a text, f47a text, f48a text, f49a text, f50a text, f51a text, f52a text,

	-- all pv2
	message_idb integer, segment_nameb text, f1b text, f2b text, f3b text, f4b text, f5b text, f6b text, f7b text, f8b text, f9b text, f10b text, f11b text, f12b text, f13b text, f14b text, f15b text, f16b text, f17b text, f18b text, 
	f19b text, f20b text, f21b text, f22b text, f23b text, f24b text, f25b text, f26b text, f27b text, f28b text, f29b text, f30b text, f31b text, f32b text, f33b text, f34b text, f35b text, f36b text, f37b text, f38b text, f39b text, 
	f40b text, f41b text, f42b text, f43b text, f44b text, f45b text, f46b text, f47b text, f48b text, f49b text, f50b text, f51b text, f52b text,

	-- all zvi
	message_idc integer, segment_namec text, f1c text, f2c text, f3c text, f4c text, f5c text, f6c text, f7c text, f8c text, f9c text, f10c text, f11c text, f12c text, f13c text, f14c text, f15c text, f16c text, f17c text, f18c text, 
	f19c text, f20c text, f21c text, f22c text, f23c text, f24c text, f25c text, f26c text, f27c text, f28c text, f29c text, f30c text, f31c text, f32c text, f33c text, f34c text, f35c text, f36c text, f37c text, f38c text, f39c text, 
	f40c text, f41c text, f42c text, f43c text, f44c text, f45c text, f46c text, f47c text, f48c text, f49c text, f50c text, f51c text, f52c text
)
as $$
begin

	return query
	select 
		m.message_id,
		mt.message_type, 
		mt.description as message_type_description, 
		to_char(m.log_date, 'YYYY-MM-DD HH:mm') as message_date, 

		-- pv1
		pv1.f2 as pv1_2_patient_class,
		split_part(pv1.f3, '^', 1) as pv1_3_patient_location,
		split_part(pv1.f7, '^', 2) || ', ' || split_part(pv1.f7, '^', 3) as pv1_7_attending_doctor,
		pv1.f10 as pv1_10_hospital_service,
		pv1.f18 as pv1_18_patient_type,
		pv1.f19 as pv1_19_attendance_number,
		pv1.f41 as pv1_41_account_status,
		case when pv1.f44 is not null and pv1.f44 != '' then to_char(to_timestamp(pv1.f44, 'YYYYMMDDHH24MISS'), 'YYYY-MM-DD HH:mm') else '' end as pv1_44_admit_date,
		case when pv1.f45 is not null and pv1.f45 != '' then to_char(to_timestamp(pv1.f45, 'YYYYMMDDHH24MISS'), 'YYYY-MM-DD HH:mm') else '' end as pv1_45_discharge_date,

		-- pv2
		case when pv2.f8 is not null and pv2.f8 != '' then to_char(to_timestamp(pv2.f8, 'YYYYMMDDHH24MISS'), 'YYYY-MM-DD HH:mm') else '' end as pv2_8_expected_admit_date,
		
		-- zvi
		case when zvi.f15 is not null and zvi.f15 != '' then to_char(to_timestamp(zvi.f15, 'YYYYMMDDHH24MISS'), 'YYYY-MM-DD HH:mm') else '' end as zvi_f15_assign_to_location_date,

		pv1.*,
		pv2.*,
		zvi.*
	from log.message m
	inner join configuration.message_type mt on m.inbound_message_type = mt.message_type
	left outer join helper.get_split_segments(m.message_id) pv1 on m.message_id = pv1.message_id and pv1.segment_name = 'PV1'
	left outer join helper.get_split_segments(m.message_id) pv2 on m.message_id = pv2.message_id and pv2.segment_name = 'PV2'
	left outer join helper.get_split_segments(m.message_id) zvi on m.message_id = zvi.message_id and zvi.segment_name = 'ZVI'
	where m.pid2 = _cnn_number
	order by m.message_id asc;

end;
$$ language plpgsql;
