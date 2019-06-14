/*
	Schema V1.7: Add outbound acknowledgement code to log.dead_letter
*/

create table dictionary.acknowledgement_code
(
	ack_code char(2) not null,
	description varchar(100) not null,
	
	constraint dictionary_acknowledgementcode_ackcode_pk primary key (ack_code),
	constraint dictionary_acknowledgementcode_ackcode_ck check ((char_length(trim(ack_code)) > 0) and (upper(ack_code) = ack_code)),
	constraint dictionary_acknowledgementcode_description_ck check (char_length(trim(description)) > 0)
);

insert into dictionary.acknowledgement_code 
(
	ack_code,
	description
)
values
('AA', 'Application Accept'),
('AE', 'Application Error'),
('AR', 'Application Reject'),
('CA', 'Commit Accept'),
('CE', 'Commit Error'),
('CR', 'Commit Reject');

alter table log.dead_letter add outbound_ack_code char(2) null;

update log.dead_letter set outbound_ack_code = 'AE';
