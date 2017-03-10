/*
	insert test data
*/

insert into configuration.channel 
(
	channel_id, 
	channel_name, 
	port_number, 
	is_active,
	use_tls, 
	sending_application,
	sending_facility,
	receiving_application,
	receiving_facility,
	pid1_field,
	pid1_assigning_auth,
	pid2_field,
	pid2_assigning_auth,
	eds_service_identifier,
	notes
)
values
(1, 'HOMERTON', 8900, true, false, 'P0241', 'HOMERTON', 'HOMERTON_TIE', 'HOMERTON', 19, null, 3, 'Homerton Case Note Number', 'RQX', ''),
(2, 'BARTS', 8901, true, false, 'BLT_TIE', 'BLT', 'EDS', 'ENDEAVOUR', null, null, null, null, 'R1H', '');

insert into configuration.channel_message_type
(
	channel_id,
	message_type,
	is_allowed
)
select
	1,
	message_type,
	true
from dictionary.message_type
order by message_type;

insert into configuration.eds
(
	single_row_lock,
	eds_url,
	software_content_type,
	software_version,
	use_keycloak,
	keycloak_token_uri,
	keycloak_realm,
	keycloak_username,
	keycloak_password,
	keycloak_clientid
)
values
(
	true,
	'http://localhost:8080/api/PostMessageAsync',
	'HL7V2',
	'1.0',
	true,
	'https://devauth.endeavourhealth.net/auth',
	'endeavour',
	'hl7user',
	'hl7password',
	'eds-hl7receiver'
);

