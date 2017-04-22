----------------------------------------------------------------------------------------------
-- set code mappings
--

do
$$
declare
	_h varchar(100);
	_code_context varchar(100);
	_target_code_system varchar(500);
begin
	_h = 'HOMERTON';

	----------------------------------------------------------------------------------------------
	-- HL7 sex -> FHIR administrative gender
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_SEX';
	_target_code_system = 'http://hl7.org/fhir/administrative-gender';
	
	perform mapping.set_code_mapping(_h, _code_context, 'male',          '', '', 'male',    _target_code_system, 'Male');
	perform mapping.set_code_mapping(_h, _code_context, 'female',        '', '', 'female',  _target_code_system, 'Female');
	perform mapping.set_code_mapping(_h, _code_context, 'unspecified',   '', '', 'unknown', _target_code_system, 'Unknown');
	perform mapping.set_code_mapping(_h, _code_context, 'not known',     '', '', 'other',   _target_code_system, 'Other');
	perform mapping.set_code_mapping(_h, _code_context, 'indeterminate', '', '', 'other',   _target_code_system, 'Other');

	----------------------------------------------------------------------------------------------
	-- HL7 name type -> FHIR name use
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_NAME_TYPE';
	_target_code_system = 'http://hl7.org/fhir/name-use';
	
	perform mapping.set_code_mapping(_h, _code_context, 'personnel', '', '', 'official', _target_code_system, 'Official');
	perform mapping.set_code_mapping(_h, _code_context, 'current',   '', '', 'official', _target_code_system, 'Official');
	perform mapping.set_code_mapping(_h, _code_context, 'alternate', '', '', 'nickname', _target_code_system, 'Nickname');
	perform mapping.set_code_mapping(_h, _code_context, 'preferred', '', '', 'usual',    _target_code_system, 'Usual');
	perform mapping.set_code_mapping(_h, _code_context, 'birth',     '', '', 'old',      _target_code_system, 'Old');
	perform mapping.set_code_mapping(_h, _code_context, 'adopted',   '', '', 'old',      _target_code_system, 'Old');
	perform mapping.set_code_mapping(_h, _code_context, 'previous',  '', '', 'old',      _target_code_system, 'Old');
	perform mapping.set_code_mapping(_h, _code_context, 'maiden',    '', '', 'maiden',   _target_code_system, 'Maiden');

	----------------------------------------------------------------------------------------------
	-- HL7 address type -> FHIR address use
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_ADDRESS_TYPE';
	_target_code_system = 'http://hl7.org/fhir/address-use';

	perform mapping.set_code_mapping(_h, _code_context, 'home',      '', '', 'home', _target_code_system, 'Home');
	perform mapping.set_code_mapping(_h, _code_context, 'business',  '', '', 'work', _target_code_system, 'Work');
	perform mapping.set_code_mapping(_h, _code_context, 'temporary', '', '', 'temp', _target_code_system, 'Temporary');
	perform mapping.set_code_mapping(_h, _code_context, 'alternate', '', '', 'temp', _target_code_system, 'Temporary');
	perform mapping.set_code_mapping(_h, _code_context, 'previous',  '', '', 'old',  _target_code_system, 'Old / Incorrect');
	perform mapping.set_code_mapping(_h, _code_context, 'birth',     '', '', 'old',  _target_code_system, 'Old / Incorrect');
	
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'mailing', '', '', 'X');

	----------------------------------------------------------------------------------------------
	-- HL7 telecom equipment type -> FHIR contact point system
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_TELECOM_EQUIPMENT_TYPE';
	_target_code_system = 'http://hl7.org/fhir/contact-point-system';
	
	perform mapping.set_code_mapping(_h, _code_context, 'tel',        '', '', 'phone', _target_code_system, 'Phone');
	perform mapping.set_code_mapping(_h, _code_context, 'email',      '', '', 'email', _target_code_system, 'Email');
	perform mapping.set_code_mapping(_h, _code_context, 'text phone', '', '', 'other', _target_code_system, 'Other');
	perform mapping.set_code_mapping(_h, _code_context, 'fax',        '', '', 'fax',   _target_code_system, 'Fax');
	
	----------------------------------------------------------------------------------------------
	-- HL7 telecom use -> FHIR contact point use
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_TELECOM_USE';
	_target_code_system = 'http://hl7.org/fhir/contact-point-use';
	
	perform mapping.set_code_mapping(_h, _code_context, 'home',             '', '', 'home',   _target_code_system, 'Home');
	perform mapping.set_code_mapping(_h, _code_context, 'home phone',       '', '', 'home',   _target_code_system, 'Home');
	perform mapping.set_code_mapping(_h, _code_context, 'mobile number',    '', '', 'mobile', _target_code_system, 'Mobile');
	perform mapping.set_code_mapping(_h, _code_context, 'pager personal',   '', '', 'mobile', _target_code_system, 'Mobile');
	perform mapping.set_code_mapping(_h, _code_context, 'pager number',     '', '', 'mobile', _target_code_system, 'Mobile');
	perform mapping.set_code_mapping(_h, _code_context, 'business',         '', '', 'work',   _target_code_system, 'Work');
	perform mapping.set_code_mapping(_h, _code_context, 'temporary',        '', '', 'temp',   _target_code_system, 'Temp');
	perform mapping.set_code_mapping(_h, _code_context, 'temp phone',       '', '', 'temp',   _target_code_system, 'Temp');
	perform mapping.set_code_mapping(_h, _code_context, 'emergency number', '', '', 'temp',   _target_code_system, 'Temp');

	----------------------------------------------------------------------------------------------
	-- HL7 primary language -> FHIR primary language
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_PRIMARY_LANGUAGE';
	_target_code_system = 'http://fhir.nhs.net/ValueSet/human-language-1';

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'english',    'en', _target_code_system, 'English');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'bengali',    'bn', _target_code_system, 'Bengali');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'polish',     'pl', _target_code_system, 'Polish');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'portuguese', 'pt', _target_code_system, 'Portuguese');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'russian',    'ru', _target_code_system, 'Russian');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'spanish',    'es', _target_code_system, 'Spanish; Castilian');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'turkish',    'tr', _target_code_system, 'Turkish');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'urdu',       'ur', _target_code_system, 'Urdu');

	----------------------------------------------------------------------------------------------
	-- HL7 ethnic group -> FHIR ethnic group
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_ETHNIC_GROUP';
	_target_code_system = 'http://endeavourhealth.org/fhir/StructureDefinition/primarycare-ethnic-category-extension';

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'english',                       'A', _target_code_system, 'British');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other white british',           'A', _target_code_system, 'British');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'irish',                         'B', _target_code_system, 'Irish');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'eastern european',              'C', _target_code_system, 'Any other White background');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'greek (inc greek cypriot)',     'C', _target_code_system, 'Any other White background');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'eastern european',              'C', _target_code_system, 'Any other White background');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'jewish',                        'C', _target_code_system, 'Any other White background');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'orthodox jew',                  'C', _target_code_system, 'Any other White background');

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'white and black caribbean',     'D', _target_code_system, 'White and Black Caribbean');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other mixed',                   'G', _target_code_system, 'Any other mixed background');

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'indian (inc british)',          'H', _target_code_system, 'Indian');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'pakistani (inc. british)',      'J', _target_code_system, 'Pakistani');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'bangladeshi (inc british)',     'K', _target_code_system, 'Bangladeshi');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other asian',                   'L', _target_code_system, 'Any other Asian background');

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'caribbean',                     'M', _target_code_system, 'Caribbean');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'african (except somali)',       'N', _target_code_system, 'African');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other black',                   'P', _target_code_system, 'Any other Black background');	
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'black british',                 'P', _target_code_system, 'Any other Black background');

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'chinese',                       'R', _target_code_system, 'Chinese');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'arab or middle eastern',        'S', _target_code_system, 'Any other ethnic group');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'any other group',               'S', _target_code_system, 'Any other ethnic group');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'turkish (inc turkish cypriot)', 'S', _target_code_system, 'Any other ethnic group');

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other not stated',              'Z', _target_code_system, 'Not stated');

	----------------------------------------------------------------------------------------------
	-- HL7 marital status -> FHIR marital status
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_MARITAL_STATUS';
	_target_code_system = 'http://hl7.org/fhir/marital-status';

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'single',                               'S', _target_code_system, 'Never Married');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'married',                              'M', _target_code_system, 'Married');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'married/civil partner',                'M', _target_code_system, 'Married');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'separation/legal separation',          'L', _target_code_system, 'Legally Separated');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'divorced/civil partnership dissolved', 'D', _target_code_system, 'Divorced');
	
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '', '', 'not known', 'X');

	----------------------------------------------------------------------------------------------
	-- HL7 religion -> FHIR religion (snomed)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_RELIGION';
	_target_code_system = 'http://snomed.info/sct';

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'church of england',    '344151000000101', _target_code_system, 'Follower of Church of England (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'methodist',            '160558004',       _target_code_system, 'Methodist, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'roman catholic',       '160540005',       _target_code_system, 'Roman Catholic, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other christians',     '160549006',       _target_code_system, 'Christian, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'muslim',               '309884000',       _target_code_system, 'Muslim, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'buddhist',             '309687009',       _target_code_system, 'Buddhist, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'none',                 '160552003',       _target_code_system, 'Not religious (finding)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'jewish',               '160543007',       _target_code_system, 'Jewish, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'christadelphian',      '160566008',       _target_code_system, 'Christadelphian, follower of religion (person)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other sects/religions', '62458008',       _target_code_system, 'Has religious belief (finding)');

	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '', '', 'not known', 'X');

	----------------------------------------------------------------------------------------------
	-- HL7 encounter class -> FHIR encounter class
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_PATIENT_CLASS';
	_target_code_system = 'http://hl7.org/fhir/encounter-class';
	
	perform mapping.set_code_mapping(_h, _code_context, 'emergency',  '', '', 'emergency',  _target_code_system, 'Emergency');
	perform mapping.set_code_mapping(_h, _code_context, 'inpatient',  '', '', 'inpatient',  _target_code_system, 'Inpatient');
	perform mapping.set_code_mapping(_h, _code_context, 'outpatient', '', '', 'outpatient', _target_code_system, 'Outpatient');
	perform mapping.set_code_mapping(_h, _code_context, 'recurring',  '', '', 'other',      _target_code_system, 'Other');
	perform mapping.set_code_mapping(_h, _code_context, 'wait list',  '', '', 'other',      _target_code_system, 'Other');
	
	----------------------------------------------------------------------------------------------
	-- HL7 account status -> FHIR encounter state
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_ACCOUNT_STATUS';
	_target_code_system = 'http://hl7.org/fhir/encounter-state';
	
	perform mapping.set_code_mapping(_h, _code_context, 'active',          '', '', 'in-progress', _target_code_system, 'In progress');
	perform mapping.set_code_mapping(_h, _code_context, 'pending arrival', '', '', 'planned',     _target_code_system, 'Planned');
	perform mapping.set_code_mapping(_h, _code_context, 'preadmit',        '', '', 'planned',     _target_code_system, 'Planned');
	perform mapping.set_code_mapping(_h, _code_context, 'discharged',      '', '', 'finished',    _target_code_system, 'Finished');
	
	----------------------------------------------------------------------------------------------
	-- HL7 patient type -> FHIR encounter type (homerton)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_PATIENT_TYPE';
	_target_code_system = 'http://endeavourhealth.org/fhir/ValueSet/encounter-type-homerton';
	
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'clinical measurement',           'clinical-measurement',           _target_code_system, 'Clinical measurement');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'clinical measurement wait list', 'clinical-measurement-wait-list', _target_code_system, 'Clinical measurement waiting list');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'day case',                       'day-case',                       _target_code_system, 'Day case');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'day case waiting list',          'day-case-wait-list',             _target_code_system, 'Day case waiting list');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'emergency department',           'emergency',                      _target_code_system, 'Emergency');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'inpatient',                      'inpatient',                      _target_code_system, 'Inpatient');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'inpatient waiting list',         'inpatient-wait-list',            _target_code_system, 'Inpatient waiting list');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'maternity',                      'maternity',                      _target_code_system, 'Maternity waiting list');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'newborn',                        'newborn',                        _target_code_system, 'Newborn');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'outpatient',                     'outpatient',                     _target_code_system, 'Outpatient');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'outpatient referral',            'outpatient-referral',            _target_code_system, 'Outpatient referral');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'radiology',                      'radiology',                      _target_code_system, 'Radiology');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'radiology referral wait list',   'radiology-wait-list',            _target_code_system, 'Radiology wait list');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'regular day admission',          'regular-day-admission',          _target_code_system, 'Regular day admission');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'dna/cancel op',                  'dna-cancel-op',                  _target_code_system, 'DNA/cancel operation');
	
	----------------------------------------------------------------------------------------------
	-- HL7 admission type -> FHIR admission type (homerton)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_ADMISSION_TYPE';
	_target_code_system = 'http://endeavourhealth.org/fhir/ValueSet/admission-type-homerton';
	
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'emergency-a&e/dental',          'emergency-ae-or-dental',        _target_code_system, 'Emergency - A&E/Dental');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'emergency-a\t\e/dental',        'emergency-ae-or-dental',        _target_code_system, 'Emergency - A&E/Dental');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'emergency-o/p clinic',          'emergency-outpatients',         _target_code_system, 'Emergency - Outpatients clinic');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'emergency-gp',                  'emergency-gp',                  _target_code_system, 'Emergency - GP');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'emergency-other',               'emergency-other',               _target_code_system, 'Emergency - Other');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'maternity-ante partum',         'maternity-ante-partum',         _target_code_system, 'Maternity - Ante partum');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'maternity-post partum',         'maternity-post-partum',         _target_code_system, 'Maternity - Post partum');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'baby born in hospital',         'baby-born-in-hospital',         _target_code_system, 'Baby born in hospital');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'baby born outside hospital',    'baby-born-outside-hospital',    _target_code_system, 'Baby born outside hospital');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'baby born at home as intended', 'baby-born-at-home-as-intended', _target_code_system, 'Baby born at home as intended');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'planned',                       'planned',                       _target_code_system, 'Planned');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'waiting list',                  'booked',                        _target_code_system, 'Booked');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'booked',                        'wait-list',                     _target_code_system, 'Waiting list');

	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '', '', 'cd:59514', 'X');
	
	----------------------------------------------------------------------------------------------
	-- HL7 discharge disposition -> FHIR discharge disposition (homerton)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_DISCHARGE_DISPOSITION';
	_target_code_system = 'http://endeavourhealth.org/fhir/ValueSet/discharge-disposition-homerton';
	
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'admitted as inpatient',                      'admitted-as-inpatient',                  _target_code_system, 'Admitted as inpatient');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'deceased',                                   'deceased',                               _target_code_system, 'Deceased');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'died in department',                         'died-in-department',                     _target_code_system, 'Died in department');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'stillbirth',                                 'stillbirth',                             _target_code_system, 'Stillbirth');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'mental health unit',                         'mental-health-unit',                     _target_code_system, 'Mental health unit');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'discharge-mental tribunal',                  'discharge-mental-tribunal',              _target_code_system, 'Discharge - mental tribunal');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'normal discharge',                           'normal-discharge',                       _target_code_system, 'Discharge - normal');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'normal discharge with follow up',            'normal-discharge-with-follow-up',        _target_code_system, 'Discharge - normal, with follow-up');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'regular discharge with follow-up',           'normal-discharge-with-follow-up',        _target_code_system, 'Discharge - normal, with follow-up');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'no follow up required',                      'no-follow-up-required',                  _target_code_system, 'Discharge - normal, no follow up required');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'discharged for other reasons',               'discharge-other-reasons',                _target_code_system, 'Discharge - other reasons');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'discharge for other reason with followup',   'discharge-other-reasons-with-follow-up', _target_code_system, 'Discharge - other reasons with follow up');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'discharge-self/relative',                    'discharge-self-or-relative',             _target_code_system, 'Discharge - self/relative');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'discharged with concern',                    'discharge-with-concern',                 _target_code_system, 'Discharge - with concern');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'left department before treatment',           'left-dept-before-treatment',             _target_code_system, 'Left department before treatment');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'left department - refused treatment',        'left-dept-refused-treatment',            _target_code_system, 'Left department - refused treatment');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'other',                                      'other',                                  _target_code_system, 'Other');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'referral to general practitioner',           'referral-to-gp',                         _target_code_system, 'Referred to general practitioner');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'referral to outpatient clinic',              'referred-to-outpatient-clinic',          _target_code_system, 'Referred to outpatient clinic');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'referred to a\t\e clinic',                   'referred-to-ae-clinic',                  _target_code_system, 'Referred to A&E clinic');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'referred to a&e clinic',                     'referred-to-ae-clinic',                  _target_code_system, 'Referred to A&E clinic');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'referred to fracture clinic',                'referred-to-fracture-clinic',            _target_code_system, 'Referred to fracture clinic');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'referred to other health care profession',   'referred-to-other-hcp',                  _target_code_system, 'Referred to other health care profession');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'transferred to other health care provider',  'transfer-to-other-hcp',                  _target_code_system, 'Transfer to other health care provider');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'transferred to other health care provide',   'transfer-to-other-hcp',                  _target_code_system, 'Transfer to other health care provider');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'walk in centre',                             'walk-in-centre',                         _target_code_system, 'Walk-in centre');

	----------------------------------------------------------------------------------------------
	-- HL7 discharge location -> FHIR discharge destination (endeavour/NHS)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_DISCHARGED_TO_LOCATION';
	_target_code_system = 'http://endeavourhealth.org/fhir/ValueSet/discharge-destination';

	perform mapping.set_code_mapping(_h, _code_context, '', '', 'usual place of residence',           '19', _target_code_system, 'Usual place of residence');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'temporary home',                     '29', _target_code_system, 'Temporary place of residence');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'repatriation from hsph',             '30', _target_code_system, 'Repatriation from high security psychiatric hospital');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'penal establishment/police station', '38', _target_code_system, 'Penal establishment or police station');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'high security psychiatric (sco)',    '48', _target_code_system, 'High security psychiatric hospital (Scotland)');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'nhs medium secure',                  '50', _target_code_system, 'NHS other Hospital Provider - medium secure unit');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'nhs provider-general',               '51', _target_code_system, 'NHS other Hospital Provider - general');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'nhs provider-maternity',             '52', _target_code_system, 'NHS other Hospital Provider - maternity');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'nhs provider-mental health',         '53', _target_code_system, 'NHS other Hodpital Provider - mental health/learning disabilities');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'nhs nursing home',                   '54', _target_code_system, 'NHS run Care Home');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'not applicable-died or stillbirth',  '79', _target_code_system, 'Not applicable - patient died or still birth');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'non-nhs residential care',           '85', _target_code_system, 'Non-NHS run Care Home');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'non-nhs hospital',                   '87', _target_code_system, 'Non-NHS run hospital');
	perform mapping.set_code_mapping(_h, _code_context, '', '', 'non-nhs hospice',                    '88', _target_code_system, 'Non-NHS run hospice');
	
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '', '', 'not known', 'X');
	
	----------------------------------------------------------------------------------------------
	-- HL7 message type code -> HL7 message type
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_MESSAGE_TYPE';
	_target_code_system = 'http://endeavourhealth.org/fhir/v2-message-type';
	
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a01', '', '', 'ADT^A01', _target_code_system, 'Admit/visit notification');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a02', '', '', 'ADT^A02', _target_code_system, 'Transfer a patient');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a03', '', '', 'ADT^A03', _target_code_system, 'Discharge/end visit');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a04', '', '', 'ADT^A04', _target_code_system, 'Register a patient');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a05', '', '', 'ADT^A05', _target_code_system, 'Pre-admit a patient');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a06', '', '', 'ADT^A06', _target_code_system, 'Change an outpatient to an inpatient');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a07', '', '', 'ADT^A07', _target_code_system, 'Change an inpatient to an outpatient');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a08', '', '', 'ADT^A08', _target_code_system, 'Update patient information');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a13', '', '', 'ADT^A13', _target_code_system, 'Cancel discharge/end visit');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a28', '', '', 'ADT^A28', _target_code_system, 'Add person information');
	perform mapping.set_code_mapping(_h, _code_context, 'adt^a31', '', '', 'ADT^A31', _target_code_system, 'Update person information');

	----------------------------------------------------------------------------------------------
	-- HL7 patient death indicator -> Deceased boolean (homerton)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_PATIENT_DEATH_INDICATOR';
	_target_code_system = 'NO-CODE-SYSTEM';

	perform mapping.set_code_mapping(_h, _code_context, 'yes', '', '', 'true', _target_code_system, 'Deceased');

	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'no', '', '', 'X');

	----------------------------------------------------------------------------------------------
	-- HL7 patient identifier type and assigning auth -> FHIR patient identifier system (endeavour)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_PATIENT_ID_TYPE_AND_ASSIGNING_AUTH';
	_target_code_system = 'NO-CODE-SYSTEM';
	
	perform mapping.set_code_mapping(_h, _code_context, 'nhs number^nhs',                '', '', 'http://fhir.nhs.net/Id/nhs-number',                                          _target_code_system, 'NHS number');
	perform mapping.set_code_mapping(_h, _code_context, 'homerton case note number^cnn', '', '', 'http://endeavourhealth.org/fhir/id/v2-local-patient-id/homerton-cnn',        _target_code_system, 'Homerton case note number');
	perform mapping.set_code_mapping(_h, _code_context, 'homerton case note number^mrn', '', '', 'http://endeavourhealth.org/fhir/id/v2-local-patient-id/homerton-mrn',        _target_code_system, 'Homerton medical record number');
	perform mapping.set_code_mapping(_h, _code_context, 'newham case note number^cnn',   '', '', 'http://endeavourhealth.org/fhir/id/v2-local-patient-id/newham-cnn',          _target_code_system, 'Newnham case note number');
	perform mapping.set_code_mapping(_h, _code_context, 'newham case note number^mrn',   '', '', 'http://endeavourhealth.org/fhir/id/v2-local-patient-id/newham-mrn',          _target_code_system, 'Newnham medical record number');
	perform mapping.set_code_mapping(_h, _code_context, 'person id^person id',           '', '', 'http://endeavourhealth.org/fhir/id/v2-local-patient-id/homerton-personid',   _target_code_system, 'Homerton person ID');

	----------------------------------------------------------------------------------------------
	-- HL7 doctor identifier type and assigning auth -> FHIR practitioner identifier system (endeavour)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_DOCTOR_ID_TYPE_AND_ASSIGNING_AUTH';
	_target_code_system = 'NO-CODE-SYSTEM';

	perform mapping.set_code_mapping(_h, _code_context, 'nhs consultant number^non gp',                              '', '', 'http://endeavourhealth.org/fhir/Identifier/consultant-code',                              _target_code_system, 'Consultant code');
	perform mapping.set_code_mapping(_h, _code_context, 'community dr nbr^community dr nbr',                         '', '', 'http://endeavourhealth.org/fhir/Identifier/consultant-code',                              _target_code_system, 'Consultant code');
	perform mapping.set_code_mapping(_h, _code_context, '^community dr nbr',                                         '', '', 'http://endeavourhealth.org/fhir/Identifier/consultant-code',                              _target_code_system, 'Consultant code');
	perform mapping.set_code_mapping(_h, _code_context, 'external id^external identifier',                           '', '', 'http://endeavourhealth.org/fhir/Identifier/gmc-number',                                   _target_code_system, 'GMC number');
	perform mapping.set_code_mapping(_h, _code_context, '^external identifier',                           '', '', 'http://endeavourhealth.org/fhir/Identifier/gmc-number',                                              _target_code_system, 'GMC number');
	perform mapping.set_code_mapping(_h, _code_context, 'personnel primary identifier^personnel primary identifier', '', '', 'http://endeavourhealth.org/fhir/id/v2-local-practitioner-id/homerton-personnelprimaryid', _target_code_system, 'Homerton primary personnel identifier');

	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '^provider num', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'nhs personnel code^external identifier', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'doctor nbr^organization doctor', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'homerton radiology id^other', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'homerton sysmed prsnl pool^other', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, 'nhs prsnl id^prsnlid', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '^other', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '^post code', '', '', 'X');
	perform mapping.set_code_mapping_action_not_mapped(_h, _code_context, '^address', '', '', 'X');

	----------------------------------------------------------------------------------------------
	-- HL7 encounter identifier type and assigning auth -> FHIR practitioner identifier system (endeavour)
	----------------------------------------------------------------------------------------------
	_code_context = 'HL7_ENCOUNTER_ID_TYPE_AND_ASSIGNING_AUTH';
	_target_code_system = 'NO-CODE-SYSTEM';

	perform mapping.set_code_mapping(_h, _code_context, 'homerton fin^encounter no.',                '', '', 'http://endeavourhealth.org/fhir/id/v2-local-episode-id/homerton-fin',          _target_code_system, 'Homerton episode identifier (FIN)');
	perform mapping.set_code_mapping(_h, _code_context, '^attendance no.',                           '', '', 'http://endeavourhealth.org/fhir/id/v2-local-episode-id/homerton-fin',          _target_code_system, 'Homerton episode identifier (FIN)');
	perform mapping.set_code_mapping(_h, _code_context, 'homerton attendance number^attendance no.', '', '', 'http://endeavourhealth.org/fhir/id/v2-local-episode-id/homerton-attendanceno', _target_code_system, 'Homerton attendance number');
	
end
$$

