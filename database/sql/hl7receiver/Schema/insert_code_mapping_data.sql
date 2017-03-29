
-- Telecom equipment type
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_EQUIPMENT_TYPE', 'tel', '', '', 'phone', 'http://hl7.org/fhir/contact-point-system', 'Phone');

-- Telecom use code
select * from mapping.set_code_mapping('HOMERTON', 'HL7_TELECOM_USE_CODE', 'mobile number', '', '', 'mobile', 'http://hl7.org/fhir/contact-point-use', 'Mobile');

-- Name type code
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PERSON_NAME_TYPE_CODE', 'personnel', '', '', 'usual', 'http://hl7.org/fhir/name-use', 'Usual');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PERSON_NAME_TYPE_CODE', 'current', '', '', 'usual', 'http://hl7.org/fhir/name-use', 'Usual');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PERSON_NAME_TYPE_CODE', 'alternate', '', '', 'nickname', 'http://hl7.org/fhir/name-use', 'Nickname');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PERSON_NAME_TYPE_CODE', 'preferred', '', '', 'nickname', 'http://hl7.org/fhir/name-use', 'Nickname');
select * from mapping.set_code_mapping('HOMERTON', 'HL7_PERSON_NAME_TYPE_CODE', 'previous', '', '', 'old', 'http://hl7.org/fhir/name-use', 'Old');
