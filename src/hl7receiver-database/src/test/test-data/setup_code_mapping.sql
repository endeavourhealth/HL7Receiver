
----------------------------------------------------------------------------------------------
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


----------------------------------------------------------------------------------------------
-- set code contexts
--
insert into mapping.code_context
(
	code_context_id,
	code_context_name,
	source_code_is_case_insensitive,
	source_term_is_case_insensitive,
	code_action_id_unmapped_default,
	message_type,
	field_locator,
	code_context_description
)
values
(1, 'HL7_SEX',                                   true, true, 'F', 'HL7 ADT', 'PID.8',     'Sex (HL7v2 table 0001)'),
(2, 'HL7_NAME_TYPE',                             true, true, 'F', 'HL7 ADT', 'XPN.7',     'Name type (HL7v2 table 0200)'),
(3, 'HL7_ADDRESS_TYPE',                          true, true, 'F', 'HL7 ADT', 'XAD.7',     'Address type (HL7v2 table 0190'),
(4, 'HL7_TELECOM_USE',                           true, true, 'F', 'HL7 ADT', 'XTN.2',     'Telecommunication use code (HL7v2 table 0201)'),
(5, 'HL7_TELECOM_EQUIPMENT_TYPE',                true, true, 'F', 'HL7 ADT', 'XTN.3',     'Telecommunication equipment type (HL7v2 table 0202)'),
(6, 'HL7_PRIMARY_LANGUAGE',                      true, true, 'F', 'HL7 ADT', 'PID.15',    'Patient primary language (HL7v2 table 0296)'),
(7, 'HL7_ETHNIC_GROUP',                          true, true, 'F', 'HL7 ADT', 'PID.22',    'Ethnic group (HL7v2 table 0189)'),
(8, 'HL7_MARITAL_STATUS',                        true, true, 'F', 'HL7 ADT', 'PID.16',    'Marital status (HL7v2 table 0002)'),
(9, 'HL7_RELIGION',                              true, true, 'F', 'HL7 ADT', 'PID.17',    'Religion (HL7v2 table 0006)'),
(10, 'HL7_PATIENT_CLASS',                        true, true, 'F', 'HL7 ADT', 'PV1.1',     'Patient class (HL7v2 table 0004)'),
(11, 'HL7_ACCOUNT_STATUS',                       true, true, 'F', 'HL7_ADT', 'PV1.41',    'Account status (HL7 table 0117)'),
(12, 'HL7_PATIENT_TYPE',                         true, true, 'F', 'HL7 ADT', 'PV1.18',    'Patient type (HL7v2 table 0018)'),
(13, 'HL7_ADMISSION_TYPE',                       true, true, 'F', 'HL7 ADT', 'PV1.4',     'Admission type (HL7v2 table 0007)'),
(14, 'HL7_DISCHARGE_DISPOSITION',                true, true, 'F', 'HL7 ADT', 'PV1.36',    'Discharge disposition (HL7 table 0112)'),
(15, 'HL7_DISCHARGED_TO_LOCATION',               true, true, 'F', 'HL7 ADT', 'PV1.37',    'Discharged to location (HL7 table 0113)'),
(16, 'HL7_MESSAGE_TYPE',                         true, true, 'F', 'HL7 ADT', 'MSH.9',     'Message type, event and structure (HL7 tables 0076, 0003, 0354)'),
(17, 'HL7_PATIENT_DEATH_INDICATOR',              true, true, 'F', 'HL7 ADT', 'PID.30',    'Yes/no indicator used as patient death indicator (HL7 table 0164, HL7 item 00741)'),
(18, 'HL7_PATIENT_ID_TYPE_AND_ASSIGNING_AUTH',   true, true, 'F', 'HL7 ADT', 'CX.4^CX.5', 'Patient identifier type and assigning authority (HL7 table 0203, HL7 table 0363)'),
(19, 'HL7_DOCTOR_ID_TYPE_AND_ASSIGNING_AUTH',    true, true, 'F', 'HL7 ADT', 'CX.4^CX.5', 'Practitioner identifier type and assigning authority (HL7 table 0203, HL7 table 0363)'),
(20, 'HL7_ENCOUNTER_ID_TYPE_AND_ASSIGNING_AUTH', true, true, 'F', 'HL7 ADT', 'CX.4^CX.5', 'Encounter identifier type and assigning authority (HL7 table 0203, HL7 table 0363)');

----------------------------------------------------------------------------------------------
-- set code systems
--
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
	7,
	'http://endeavourhealth.org/fhir/StructureDefinition/primarycare-ethnic-category-extension',
	'Ethnic category (Endeavour/NHS)',
	'Uses the following codes http://www.datadictionary.nhs.uk/data_dictionary/attributes/e/end/ethnic_category_code_de.asp',
	'A, B, C, S, Z'
),
(
	8,
	'http://hl7.org/fhir/marital-status',
	'Marital status',
	'See http://hl7.org/fhir/DSTU2/valueset-marital-status.html',
	'A, D, M, S'
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
	'See http://hl7.org/fhir/DSTU2/valueset-encounter-state.html',
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
	'http://endeavourhealth.org/fhir/ValueSet/discharge-destination',
	'Discharge destination (Endeavour/NHS)',
	'See http://www.datadictionary.nhs.uk/data_dictionary/attributes/d/disc/discharge_destination_de.asp',
	'19, 29, 30, 53'
),
(
	16,
	'http://endeavourhealth.org/fhir/v2-message-type',
	'HL7v2 message type',
	'Combined message type and trigger event',
	'ADT^A01, ADT^A09, ADT^A18'
),
(
	1000,
	'http://snomed.info/sct',
	'SNOMED CT',
	'See http://browser.ihtsdotools.org/',
	'404684003, 307824009, 308643009'
);
