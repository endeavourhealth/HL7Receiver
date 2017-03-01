create table log.test_transform
(
	message_id integer not null,
	hl7_payload varchar not null,
	fhir_payload varchar null,
	error_message varchar null,
	
	constraint log_test_transform_messageid_pk primary key (message_id)
);


