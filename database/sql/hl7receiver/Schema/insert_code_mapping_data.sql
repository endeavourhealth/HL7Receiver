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
	'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton',
	'Admission type (Homerton)',
	'',
	'emergency-outpatients, maternity-ante-partum, planned'
),
(
	14,
	'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton',
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
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'birth', '', '', 'old', 'http://hl7.org/fhir/name-use', 'Old');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'adopted', '', '', 'old', 'http://hl7.org/fhir/name-use', 'Old');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_NAME_TYPE', 'previous', '', '', 'old', 'http://hl7.org/fhir/name-use', 'Old');

-- v2 address type -> fhir address use
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADDRESS_TYPE', 'home', '', '', 'home', 'http://hl7.org/fhir/address-use', 'Home');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADDRESS_TYPE', 'previous', '', '', 'old', 'http://hl7.org/fhir/address-use', 'Old');

-- v2 telecom equipment type -> fhir contact point system
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'tel', '', '', 'phone', 'http://hl7.org/fhir/contact-point-system', 'Phone');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'email', '', '', 'email', 'http://hl7.org/fhir/contact-point-system', 'Email');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'text phone', '', '', 'other', 'http://hl7.org/fhir/contact-point-system', 'Other');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'fax', '', '', 'Fax', 'http://hl7.org/fhir/contact-point-system', 'Fax');

-- v2 telecom use -> fhir contact point use
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_USE', 'mobile number', '', '', 'mobile', 'http://hl7.org/fhir/contact-point-use', 'Mobile');

-- v2 encounter class -> fhir encounter class
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_CLASS', 'emergency', '', '', 'emergency', 'http://hl7.org/fhir/encounter-class', 'Emergency');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_CLASS', 'inpatient', '', '', 'inpatient', 'http://hl7.org/fhir/encounter-class', 'Inpatient');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_CLASS', 'outpatient', '', '', 'outpatient', 'http://hl7.org/fhir/encounter-class', 'Outpatient');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_CLASS', 'recurring', '', '', 'other', 'http://hl7.org/fhir/encounter-class', 'Other');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_CLASS', 'wait list', '', '', 'other', 'http://hl7.org/fhir/encounter-class', 'Other');

-- v2 account status -> fhir encounter state
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'active', '', '', 'in-progress', 'http://hl7.org/fhir/encounter-state', 'In progress');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'preadmit', '', '', 'planned', 'http://hl7.org/fhir/encounter-state', 'Planned');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'discharged', '', '', 'finished', 'http://hl7.org/fhir/encounter-state', 'Finished');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ACCOUNT_STATUS', 'cancelled', '', '', 'cancelled', 'http://hl7.org/fhir/encounter-state', 'Cancelled');

-- v2 patient type -> fhir encounter type (homerton)
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'clinical measurement', '', '', 'clinical-measurement', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Clinical measurement');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'clinical measurement wait list', '', '', 'clinical-measurement-wait-list', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Clinical measurement waiting list');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'day case', '', '', 'day-case', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Day case');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'day case waiting list', '', '', 'day-case-wait-list', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Day case waiting list');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'emergency department', '', '', 'emergency', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Emergency');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'inpatient', '', '', 'inpatient', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Inpatient');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'inpatient waiting list', '', '', 'inpatient-wait-list', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Inpatient waiting list');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'maternity', '', '', 'maternity', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Maternity waiting list');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'newborn', '', '', 'newborn', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Newborn');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'outpatient', '', '', 'outpatient', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Outpatient');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'outpatient referral', '', '', 'outpatient-referral', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Outpatient referral');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'radiology', '', '', 'radiology', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Radiology');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'radiology referral wait list', '', '', 'radiology-wait-list', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Radiology wait list');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PATIENT_TYPE', 'regular day admission', '', '', 'regular-day-admission', 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton', 'Regular day admission');

-- v2 admission type -> fhir admission type (homerton)
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'emergency-a&e/dental', '', '', 'emergency-ae-or-dental', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Emergency - A&E/Dental');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'emergency-a\t\e/dental', '', '', 'emergency-ae-or-dental', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Emergency - A&E/Dental');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'emergency-o/p clinic', '', '', 'emergency-outpatients', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Emergency - Outpatients clinic');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'emergency-other', '', '', 'emergency-other', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Emergency - Other');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'maternity-ante partum', '', '', 'maternity-ante-partum', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Maternity - Ante Partum');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'maternity-post partum', '', '', 'maternity-post-partum', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Maternity - Post Partum');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'baby born in hospital', '', '', 'baby-born', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Baby Born in Hospital');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'planned', '', '', 'planned', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Planned');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'waiting list', '', '', 'booked', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Booked');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_ADMISSION_TYPE', 'booked', '', '', 'wait-list', 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton', 'Waiting List');

-- v2 discharge disposition -> fhir discharge disposition (homerton)
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'admitted as inpatient', '', '', 'admitted-as-inpatient', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Admitted as inpatient');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'deceased', '', '', 'deceased', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Deceased');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'discharge-mental tribunal', '', '', 'discharge-mental-tribunal', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Discharge - mental tribunal');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'normal discharge', '', '', 'normal-discharge', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Discharge - normal');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'normal discharge with follow up', '', '', 'normal-discharge-with-follow-up', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Discharge - normal, with follow-up');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'regular discharge with follow-up', '', '', 'normal-discharge-with-follow-up', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Discharge - normal, with follow-up');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'no follow up required', '', '', 'no-follow-up-required', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Discharge - normal, no follow up required');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'discharge-self/relative', '', '', 'discharge-self-or-relative', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Discharge - self/relative');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'left department before treatment', '', '', 'left-dept-before-treatment', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Left department before treatment');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'other', '', '', 'other', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Other');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'referral to general practitioner', '', '', 'referral-to-gp', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Referred to general practitioner');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'referral to outpatient clinic', '', '', 'referred-to-outpatient-clinic', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Referred to outpatient clinic');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'referred to a\t\e clinic', '', '', 'referred-to-ae-clinic', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Referred to A&E clinic');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'referred to a&e clinic', '', '', 'referred-to-ae-clinic', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Referred to A&E clinic');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'referred to fracture clinic', '', '', 'referred-to-fracture-clinic', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Referred to fracture clinic');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'referred to other health care profession', '', '', 'referred-to-other-hcp', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Referred to other health care profession');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_DISCHARGE_DISPOSITION', 'transferred to other health care provider', '', '', 'transfer-to-other-hcp', 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton', 'Transfer to other health care provider');




