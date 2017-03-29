/*
	delete from mapping.code;
	delete from mapping.code_context;
	delete from mapping.code_system;
	delete from mapping.code_origin;
	
	select * from mapping.code order by code_id;
	select * from mapping.code_context;
	select * from mapping.code_system;	
	
	update log.message set next_attempt_date = now(), message_status_id = 0, message_status_date = now();
	delete from log.message_processing_content;
	
	select * from log.message;
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
(6, 'HL7_PRIMARY_LANGUAGE', true, 'F', 'HL7 ADT', 'PID.15', 'Patient primary language (HL7v2 table 0296)'),
(10, 'HL7_PATIENT_CLASS', true, 'F', 'HL7 ADT', 'PV1.1', 'Patient class (HL7v2 table 0004)'),
(11, 'HL7_ACCOUNT_STATUS', true, 'F', 'HL7_ADT', 'PV1.41', 'Account status (HL7 table 0117)'),
(12, 'HL7_PATIENT_TYPE', true, 'F', 'HL7 ADT', 'PV1.18', 'Patient type (HL7v2 table 0018)'),
(13, 'HL7_ADMISSION_TYPE', true, 'F', 'HL7 ADT', 'PV1.4', 'Admission type (HL7v2 table 0007)'),
(14, 'HL7_DISCHARGE_DISPOSITION', true, 'F', 'HL7_ADT', 'PV1.36', 'Discharge disposition (HL7 table 0112)'),
(15, 'HL7_EVENT_TYPE', true, 'F', 'HL7_ADT', 'MSH.9', 'Event type (HL7 table 0003)');

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
	'Administrative gender',
	'See http://hl7.org/fhir/DSTU2/valueset-administrative-gender.html',
	'male, female, other'
),
(
	2,
	'http://hl7.org/fhir/name-use',
	'Name use',
	'See http://hl7.org/fhir/DSTU2/valueset-name-use.html',
	'usual, official, nickname'
),
(
	3,
	'http://hl7.org/fhir/address-use',
	'Address use',
	'See http://hl7.org/fhir/DSTU2/valueset-address-use.html',
	'home, work, temp'
),
(
	4,
	'http://hl7.org/fhir/contact-point-system',
	'Contact point system',
	'See http://hl7.org/fhir/DSTU2/valueset-contact-point-system.html',
	'phone, fax, email'
),
(
	5,
	'http://hl7.org/fhir/contact-point-use',
	'Contact point use',
	'See http://hl7.org/fhir/DSTU2/valueset-contact-point-use.html',
	'home, work, temp, mobile'
),
(
	6,
	'http://fhir.nhs.net/ValueSet/human-language-1',
	'Human language (NHS)',
	'See http://www.datadictionary.nhs.uk/data_dictionary/attributes/l/language_code_de.asp',
	'en, fr, de, q1'
),
(
	10,
	'http://hl7.org/fhir/encounter-class',
	'Encounter class',
	'See http://hl7.org/fhir/DSTU2/valueset-encounter-class.html',
	'inpatient, outpatient, emergency'
),
(
	11,
	'http://hl7.org/fhir/encounter-state',
	'Encounter state',
	'http://hl7.org/fhir/DSTU2/valueset-encounter-state.html',
	'planned, in-progress, finished'
),
(
	12,
	'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton',
	'Encounter type (Homerton)',
	'',
	'clinical measurement, inpatient, maternity'
),
(
	13,
	'http://endeavourhealth.org/fhir/ValueSet/encounter-admission-type-homerton',
	'Admission type (Homerton)',
	'',
	'emergency-outpatients, maternity-ante-partum, planned'
),
(
	14,
	'http://endeavourhealth.org/fhir/ValueSet/encounter-discharge-disposition-homerton',
	'Discharge disposition (Homerton)',
	'',
	'admitted-as-inpatient, deceased, discharge-normal, referred-to-other-hcp'
),
(
	15,
	'http://hl7.org/fhir/v2/0003',
	'v2 Event type',
	'See https://www.hl7.org/fhir/v2/0003/index.html',
	'ADT/ACK - Admit/visit notification, ADT/ACK - Change an inpatient to an outpatient'
);

---------------------------------------------------------------
-- set code mappings
--

-- v2 sex -> fhir administrative gender
select * from mapping.set_code_mapping('HOMERTON', 'HL7_SEX', 'male', '', '', 'male', 'http://hl7.org/fhir/administrative-gender', 'Male');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_SEX', 'female', '', '', 'female', 'http://hl7.org/fhir/administrative-gender', 'Female');

-- v2 name type -> fhir name use
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'personnel', '', '', 'usual', 'http://hl7.org/fhir/name-use', 'Usual');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'current', '', '', 'usual', 'http://hl7.org/fhir/name-use', 'Usual');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'alternate', '', '', 'nickname', 'http://hl7.org/fhir/name-use', 'Nickname');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'preferred', '', '', 'nickname', 'http://hl7.org/fhir/name-use', 'Nickname');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'previous', '', '', 'old', 'http://hl7.org/fhir/name-use', 'Old');

-- v2 address type -> fhir address use
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADDRESS_TYPE', 'home', '', '', 'home', 'http://hl7.org/fhir/address-use', 'Home');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADDRESS_TYPE', 'previous', '', '', 'old', 'http://hl7.org/fhir/address-use', 'Old');

-- v2 telecom equipment type -> fhir contact point system
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'tel', '', '', 'phone', 'http://hl7.org/fhir/contact-point-system', 'Phone');

-- v2 telecom use -> fhir contact point use
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_USE', 'mobile number', '', '', 'mobile', 'http://hl7.org/fhir/contact-point-use', 'Mobile');

-- v2 account status -> fhir encounter state
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'active', '', '', 'in-progress', 'http://hl7.org/fhir/encounter-state', 'In progress');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'preadmit', '', '', 'planned', 'http://hl7.org/fhir/encounter-state', 'Planned');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'discharged', '', '', 'finished', 'http://hl7.org/fhir/encounter-state', 'Finished');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'cancelled', '', '', 'cancelled', 'http://hl7.org/fhir/encounter-state', 'Cancelled');

