
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
	source_term_is_case_insensitive,
	code_action_id_unmapped_default,
	message_type,
	field_locator,
	code_context_description
)
values
(1, 'HL7_SEX', true, true, 'F', 'HL7 ADT', 'PID.8', 'Sex (HL7v2 table 0001)'),
(2, 'HL7_NAME_TYPE', true, true, 'F', 'HL7 ADT', 'XPN.7', 'Name type (HL7v2 table 0200)'),
(3, 'HL7_ADDRESS_TYPE', true, true, 'F', 'HL7 ADT', 'XAD.7', 'Address type (HL7v2 table 0190'),
(4, 'HL7_TELECOM_USE', true, true, 'F', 'HL7 ADT', 'XTN.2', 'Telecommunication use code (HL7v2 table 0201)'),
(5, 'HL7_TELECOM_EQUIPMENT_TYPE', true, true, 'F', 'HL7 ADT', 'XTN.3', 'Telecommunication equipment type (HL7v2 table 0202)'),
(6, 'HL7_PRIMARY_LANGUAGE', true, true, 'F', 'HL7 ADT', 'PID.15', 'Patient primary language (HL7v2 table 0296)'),
(10, 'HL7_PATIENT_CLASS', true, true, 'F', 'HL7 ADT', 'PV1.1', 'Patient class (HL7v2 table 0004)'),
(11, 'HL7_ACCOUNT_STATUS', true, true, 'F', 'HL7_ADT', 'PV1.41', 'Account status (HL7 table 0117)'),
(12, 'HL7_PATIENT_TYPE', true, true, 'F', 'HL7 ADT', 'PV1.18', 'Patient type (HL7v2 table 0018)'),
(13, 'HL7_ADMISSION_TYPE', true, true, 'F', 'HL7 ADT', 'PV1.4', 'Admission type (HL7v2 table 0007)'),
(14, 'HL7_DISCHARGE_DISPOSITION', true, true, 'F', 'HL7_ADT', 'PV1.36', 'Discharge disposition (HL7 table 0112)'),
(15, 'HL7_EVENT_TYPE', true, true, 'F', 'HL7_ADT', 'MSH.9', 'Event type (HL7 table 0003)');


---------------------------------------------------------------
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

do
$$
declare
	_h varchar(100);
	_fhir_gender varchar(500);
	_fhir_nameuse varchar(500);
	_fhir_addressuse varchar(500);
	_fhir_contactpointsystem varchar(500);
	_fhir_contactpointuse varchar(500);
	_fhir_encounterclass varchar(500);
	_fhir_encounterstate varchar(500);
	_fhir_encountertypehomerton varchar(500);
	_fhir_admissiontypehomerton varchar(500);
	_fhir_dischargedispositionhomerton varchar(500);
begin
	_h = 'HOMERTON';

	------------------------------------------------------------
	-- v2 sex -> fhir administrative gender
	------------------------------------------------------------
	_fhir_gender = 'http://hl7.org/fhir/administrative-gender';
	
	perform mapping.set_code_mapping(_h, 'HL7_SEX', 'male',          '', '', 'male',    _fhir_gender, 'Male');
	perform mapping.set_code_mapping(_h, 'HL7_SEX', 'female',        '', '', 'female',  _fhir_gender, 'Female');
	perform mapping.set_code_mapping(_h, 'HL7_SEX', 'unspecified',   '', '', 'unknown', _fhir_gender, 'Unknown');
	perform mapping.set_code_mapping(_h, 'HL7_SEX', 'not known',     '', '', 'other',   _fhir_gender, 'Other');
	perform mapping.set_code_mapping(_h, 'HL7_SEX', 'indeterminate', '', '', 'other',   _fhir_gender, 'Other');

	------------------------------------------------------------
	-- v2 name type -> fhir name use
	------------------------------------------------------------
	_fhir_nameuse = 'http://hl7.org/fhir/name-use';
	
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'personnel', '', '', 'official',    _fhir_nameuse, 'Official');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'current',   '', '', 'official',    _fhir_nameuse, 'Official');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'alternate', '', '', 'nickname',    _fhir_nameuse, 'Nickname');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'preferred', '', '', 'usual',       _fhir_nameuse, 'Usual');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'birth',     '', '', 'old',         _fhir_nameuse, 'Old');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'adopted',   '', '', 'old',         _fhir_nameuse, 'Old');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'previous',  '', '', 'old',         _fhir_nameuse, 'Old');
	perform from mapping.set_code_mapping(_h, 'HL7_NAME_TYPE', 'maiden',    '', '', 'maiden',      _fhir_nameuse, 'Maiden');

	------------------------------------------------------------
	-- v2 address type -> fhir address use
	------------------------------------------------------------
	_fhir_addressuse = 'http://hl7.org/fhir/address-use';

	perform mapping.set_code_mapping(_h, 'HL7_ADDRESS_TYPE', 'home',      '', '', 'home', _fhir_addressuse, 'Home');
	perform mapping.set_code_mapping(_h, 'HL7_ADDRESS_TYPE', 'temporary', '', '', 'temp', _fhir_addressuse, 'Temporary');
	perform mapping.set_code_mapping(_h, 'HL7_ADDRESS_TYPE', 'alternate', '', '', 'temp', _fhir_addressuse, 'Temporary');
	perform mapping.set_code_mapping(_h, 'HL7_ADDRESS_TYPE', 'previous',  '', '', 'old',  _fhir_addressuse, 'Old / Incorrect');
	perform mapping.set_code_mapping(_h, 'HL7_ADDRESS_TYPE', 'birth',     '', '', 'old',  _fhir_addressuse, 'Old / Incorrect');
	
	perform mapping.set_code_mapping_action_not_mapped(_h, 'HL7_ADDRESS_TYPE', 'mailing', '', '', 'X');

	------------------------------------------------------------
	-- v2 telecom equipment type -> fhir contact point system
	------------------------------------------------------------
	_fhir_contactpointsystem = 'http://hl7.org/fhir/contact-point-system';
	
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_EQUIPMENT_TYPE', 'tel',        '', '', 'phone', _fhir_contactpointsystem, 'Phone');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_EQUIPMENT_TYPE', 'email',      '', '', 'email', _fhir_contactpointsystem, 'Email');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_EQUIPMENT_TYPE', 'text phone', '', '', 'other', _fhir_contactpointsystem, 'Other');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_EQUIPMENT_TYPE', 'fax',        '', '', 'fax',   _fhir_contactpointsystem, 'Fax');
	
	------------------------------------------------------------
	-- v2 telecom use -> fhir contact point use
	------------------------------------------------------------
	_fhir_contactpointuse = 'http://hl7.org/fhir/contact-point-use';
	
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'home',             '', '', 'home',   _fhir_contactpointuse, 'Home');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'home phone',       '', '', 'home',   _fhir_contactpointuse, 'Home');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'mobile number',    '', '', 'mobile', _fhir_contactpointuse, 'Mobile');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'pager personal',   '', '', 'mobile', _fhir_contactpointuse, 'Mobile');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'pager number',     '', '', 'mobile', _fhir_contactpointuse, 'Mobile');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'business',         '', '', 'work',   _fhir_contactpointuse, 'Work');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'temporary',        '', '', 'temp',   _fhir_contactpointuse, 'Temp');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'temp phone',       '', '', 'temp',   _fhir_contactpointuse, 'Temp');
	perform from mapping.set_code_mapping(_h, 'HL7_TELECOM_USE', 'emergency number', '', '', 'temp',   _fhir_contactpointuse, 'Temp');
	
	------------------------------------------------------------
	-- v2 encounter class -> fhir encounter class
	------------------------------------------------------------
	_fhir_encounterclass = 'http://hl7.org/fhir/encounter-class';
	
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_CLASS', 'emergency',  '', '', 'emergency',  _fhir_encounterclass, 'Emergency');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_CLASS', 'inpatient',  '', '', 'inpatient',  _fhir_encounterclass, 'Inpatient');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_CLASS', 'outpatient', '', '', 'outpatient', _fhir_encounterclass, 'Outpatient');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_CLASS', 'recurring',  '', '', 'other',      _fhir_encounterclass, 'Other');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_CLASS', 'wait list',  '', '', 'other',      _fhir_encounterclass, 'Other');
	
	------------------------------------------------------------
	-- v2 account status -> fhir encounter state
	------------------------------------------------------------
	_fhir_encounterstate = 'http://hl7.org/fhir/encounter-state';
	
	perform from mapping.set_code_mapping(_h, 'HL7_ACCOUNT_STATUS', 'active',          '', '', 'in-progress', _fhir_encounterstate, 'In progress');
	perform from mapping.set_code_mapping(_h, 'HL7_ACCOUNT_STATUS', 'pending arrival', '', '', 'planned',     _fhir_encounterstate, 'Planned');
	perform from mapping.set_code_mapping(_h, 'HL7_ACCOUNT_STATUS', 'preadmit',        '', '', 'planned',     _fhir_encounterstate, 'Planned');
	perform from mapping.set_code_mapping(_h, 'HL7_ACCOUNT_STATUS', 'discharged',      '', '', 'finished',    _fhir_encounterstate, 'Finished');
	
	------------------------------------------------------------
	-- v2 patient type -> fhir encounter type (homerton)
	------------------------------------------------------------
	_fhir_encountertypehomerton = 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton';
	
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'clinical measurement',           'clinical-measurement',           _fhir_encountertypehomerton, 'Clinical measurement');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'clinical measurement wait list', 'clinical-measurement-wait-list', _fhir_encountertypehomerton, 'Clinical measurement waiting list');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'day case',                       'day-case',                       _fhir_encountertypehomerton, 'Day case');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'day case waiting list',          'day-case-wait-list',             _fhir_encountertypehomerton, 'Day case waiting list');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'emergency department',           'emergency',                      _fhir_encountertypehomerton, 'Emergency');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'inpatient',                      'inpatient',                      _fhir_encountertypehomerton, 'Inpatient');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'inpatient waiting list',         'inpatient-wait-list',            _fhir_encountertypehomerton, 'Inpatient waiting list');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'maternity',                      'maternity',                      _fhir_encountertypehomerton, 'Maternity waiting list');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'newborn',                        'newborn',                        _fhir_encountertypehomerton, 'Newborn');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'outpatient',                     'outpatient',                     _fhir_encountertypehomerton, 'Outpatient');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'outpatient referral',            'outpatient-referral',            _fhir_encountertypehomerton, 'Outpatient referral');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'radiology',                      'radiology',                      _fhir_encountertypehomerton, 'Radiology');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'radiology referral wait list',   'radiology-wait-list',            _fhir_encountertypehomerton, 'Radiology wait list');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'regular day admission',          'regular-day-admission',          _fhir_encountertypehomerton, 'Regular day admission');
	perform from mapping.set_code_mapping(_h, 'HL7_PATIENT_TYPE', '', '', 'dna/cancel op',                  'dna-cancel-op',                  _fhir_encountertypehomerton, 'DNA/cancel operation');
	
	------------------------------------------------------------
	-- v2 admission type -> fhir admission type (homerton)
	------------------------------------------------------------
	_fhir_admissiontypehomerton = 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton';
	
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'emergency-a&e/dental',       'emergency-ae-or-dental',     _fhir_admissiontypehomerton, 'Emergency - A&E/Dental');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'emergency-a\t\e/dental',     'emergency-ae-or-dental',     _fhir_admissiontypehomerton, 'Emergency - A&E/Dental');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'emergency-o/p clinic',       'emergency-outpatients',      _fhir_admissiontypehomerton, 'Emergency - Outpatients clinic');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'emergency-gp',               'emergency-gp',               _fhir_admissiontypehomerton, 'Emergency - GP');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'emergency-other',            'emergency-other',            _fhir_admissiontypehomerton, 'Emergency - Other');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'maternity-ante partum',      'maternity-ante-partum',      _fhir_admissiontypehomerton, 'Maternity - Ante partum');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'maternity-post partum',      'maternity-post-partum',      _fhir_admissiontypehomerton, 'Maternity - Post partum');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'baby born in hospital',      'baby-born-in-hospital',      _fhir_admissiontypehomerton, 'Baby born in hospital');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'baby born outside hospital', 'baby-born-outside-hospital', _fhir_admissiontypehomerton, 'Baby born outside hospital');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'planned',                    'planned',                    _fhir_admissiontypehomerton, 'Planned');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'waiting list',               'booked',                     _fhir_admissiontypehomerton, 'Booked');
	perform from mapping.set_code_mapping(_h, 'HL7_ADMISSION_TYPE', '', '', 'booked',                     'wait-list',                  _fhir_admissiontypehomerton, 'Waiting list');

	perform mapping.set_code_mapping_action_not_mapped(_h, 'HL7_ADMISSION_TYPE', '', '', 'cd:59514', 'X');
	
	------------------------------------------------------------
	-- v2 discharge disposition -> fhir discharge disposition (homerton)
	------------------------------------------------------------
	_fhir_dischargedispositionhomerton = 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton';
	
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'admitted as inpatient',                      'admitted-as-inpatient',                  _fhir_dischargedispositionhomerton, 'Admitted as inpatient');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'deceased',                                   'deceased',                               _fhir_dischargedispositionhomerton, 'Deceased');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'died in department',                         'died-in-department',                     _fhir_dischargedispositionhomerton, 'Died in department');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'stillbirth',                                 'stillbirth',                             _fhir_dischargedispositionhomerton, 'Stillbirth');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'mental health unit',                         'mental-health-unit',                     _fhir_dischargedispositionhomerton, 'Mental health unit');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'discharge-mental tribunal',                  'discharge-mental-tribunal',              _fhir_dischargedispositionhomerton, 'Discharge - mental tribunal');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'normal discharge',                           'normal-discharge',                       _fhir_dischargedispositionhomerton, 'Discharge - normal');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'normal discharge with follow up',            'normal-discharge-with-follow-up',        _fhir_dischargedispositionhomerton, 'Discharge - normal, with follow-up');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'regular discharge with follow-up',           'normal-discharge-with-follow-up',        _fhir_dischargedispositionhomerton, 'Discharge - normal, with follow-up');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'no follow up required',                      'no-follow-up-required',                  _fhir_dischargedispositionhomerton, 'Discharge - normal, no follow up required');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'discharged for other reasons',               'discharge-other-reasons',                _fhir_dischargedispositionhomerton, 'Discharge - other reasons');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'discharge for other reason with followup',   'discharge-other-reasons-with-follow-up', _fhir_dischargedispositionhomerton, 'Discharge - other reasons with follow up');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'discharge-self/relative',                    'discharge-self-or-relative',             _fhir_dischargedispositionhomerton, 'Discharge - self/relative');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'discharged with concern',                    'discharge-with-concern',                 _fhir_dischargedispositionhomerton, 'Discharge - with concern');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'left department before treatment',           'left-dept-before-treatment',             _fhir_dischargedispositionhomerton, 'Left department before treatment');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'left department - refused treatment',        'left-dept-refused-treatment',            _fhir_dischargedispositionhomerton, 'Left department - refused treatment');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'other',                                      'other',                                  _fhir_dischargedispositionhomerton, 'Other');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'referral to general practitioner',           'referral-to-gp',                         _fhir_dischargedispositionhomerton, 'Referred to general practitioner');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'referral to outpatient clinic',              'referred-to-outpatient-clinic',          _fhir_dischargedispositionhomerton, 'Referred to outpatient clinic');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'referred to a\t\e clinic',                   'referred-to-ae-clinic',                  _fhir_dischargedispositionhomerton, 'Referred to A&E clinic');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'referred to a&e clinic',                     'referred-to-ae-clinic',                  _fhir_dischargedispositionhomerton, 'Referred to A&E clinic');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'referred to fracture clinic',                'referred-to-fracture-clinic',            _fhir_dischargedispositionhomerton, 'Referred to fracture clinic');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'referred to other health care profession',   'referred-to-other-hcp',                  _fhir_dischargedispositionhomerton, 'Referred to other health care profession');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'transferred to other health care provider',  'transfer-to-other-hcp',                  _fhir_dischargedispositionhomerton, 'Transfer to other health care provider');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'transferred to other health care provide',   'transfer-to-other-hcp',                  _fhir_dischargedispositionhomerton, 'Transfer to other health care provider');
	perform from mapping.set_code_mapping(_h, 'HL7_DISCHARGE_DISPOSITION', '', '', 'walk in centre',                             'walk-in-centre',                         _fhir_dischargedispositionhomerton, 'Walk-in centre');

end
$$
