/*
	delete from mapping.code;
	delete from mapping.code_context;
	delete from mapping.code_system;
	delete from mapping.code_origin;
	
	select * from mapping.code;	
	select * from mapping.code_context;
	select * from mapping.code_system;	
	
	update log.message set next_attempt_date = now(), message_status_id = 0, message_status_date = now();
	delete from log.message_processing_content;
	
	select * from log.message_processing_content;
*/

---------------------------------------------------------------
-- set code origins
--
insert into mapping.code_origin
(
	code_origin_id,
	code_origin_name,
	code_origin_description,
	hl7_channel_id
)
values
('H', 'HOMERTON', 'Homerton ADT feed', 1);


---------------------------------------------------------------
-- set code contexts
--
insert into mapping.code_context
(
	code_context_id,
	code_context_name,
	source_code_is_case_insensitive,
	code_action_id_unmapped_default,
	message_type,
	field_locator,
	code_context_description
)
values
(1, 'HL7_SEX', true, 'F', 'HL7 ADT', 'PID.8', 'Sex (HL7v2 table 0001)'),
(2, 'HL7_NAME_TYPE', true, 'F', 'HL7 ADT', 'XPN.7', 'Name type (HL7v2 table 0200)'),
(3, 'HL7_ADDRESS_TYPE', true, 'F', 'HL7 ADT', 'XAD.7', 'Address type (HL7v2 table 0190'),
(4, 'HL7_TELECOM_USE', true, 'F', 'HL7 ADT', 'XTN.2', 'Telecommunication use code (HL7v2 table 0201)'),
(5, 'HL7_TELECOM_EQUIPMENT_TYPE', true, 'F', 'HL7 ADT', 'XTN.3', 'Telecommunication equipment type (HL7v2 table 0202)'),
(6, 'HL7_PRIMARY_LANGUAGE', true, 'F', 'HL7 ADT', 'PID.15', 'Patient primary language (HL7v2 table 0296)');

insert into mapping.code_system
(
	code_system_id,
	code_system_identifier,
	code_system_friendly_name,
	code_system_description,
	code_system_examples
)
values
(
	-1,
	'NO-CODE-SYSTEM',
	'No code system',
	'Used when the is no source code system to prevent null and problems with indexes in mapping.code table',
	'(none)'
),
(
	1,
	'http://hl7.org/fhir/administrative-gender',
	'Administrative gender (FHIR)',
	'See http://hl7.org/fhir/DSTU2/valueset-administrative-gender.html',
	'male, female, other'
),
(
	2,
	'http://hl7.org/fhir/name-use',
	'Name use (FHIR)',
	'See http://hl7.org/fhir/DSTU2/valueset-name-use.html',
	'usual, official, nickname'
),
(
	3,
	'http://hl7.org/fhir/address-use',
	'Address use (FHIR)',
	'See http://hl7.org/fhir/DSTU2/valueset-address-use.html',
	'home, work, temp'
),
(
	4,
	'http://hl7.org/fhir/contact-point-system',
	'Contact point system (FHIR)',
	'See http://hl7.org/fhir/DSTU2/valueset-contact-point-system.html',
	'phone, fax, email'
),
(
	5,
	'http://hl7.org/fhir/contact-point-use',
	'Contact point use (FHIR)',
	'See http://hl7.org/fhir/DSTU2/valueset-contact-point-use.html',
	'home, work, temp, mobile'
),
(
	6,
	'http://fhir.nhs.net/ValueSet/human-language-1',
	'Human language (FHIR)',
	'See http://www.datadictionary.nhs.uk/data_dictionary/attributes/l/language_code_de.asp',
	'en, fr, de, q1'
);

---------------------------------------------------------------
-- set code mappings
--

-- sex
select * from mapping.set_code_mapping('HOMERTON', 'HL7_SEX', 'male', '', '', 'male', 'http://hl7.org/fhir/administrative-gender', 'Male');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_SEX', 'female', '', '', 'female', 'http://hl7.org/fhir/administrative-gender', 'Female');

-- name type code
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'personnel', '', '', 'usual', 'http://hl7.org/fhir/name-use', 'Usual');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'current', '', '', 'usual', 'http://hl7.org/fhir/name-use', 'Usual');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'alternate', '', '', 'nickname', 'http://hl7.org/fhir/name-use', 'Nickname');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'preferred', '', '', 'nickname', 'http://hl7.org/fhir/name-use', 'Nickname');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'previous', '', '', 'old', 'http://hl7.org/fhir/name-use', 'Old');

-- telecom equipment type
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'tel', '', '', 'phone', 'http://hl7.org/fhir/contact-point-system', 'Phone');

-- telecom use code
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_USE', 'mobile number', '', '', 'mobile', 'http://hl7.org/fhir/contact-point-use', 'Mobile');




