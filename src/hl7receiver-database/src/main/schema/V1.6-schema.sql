/*
	Schema V1.6: Create organisation mapping tables (to hold a cache of looked up values)
*/

create table mapping.organisation_type
(
	organisation_type char(2) not null,
	name varchar(100) not null,
	
	constraint mapping_organisationtype_organisationtype_pk primary key (organisation_type),
	constraint mapping_organisationtype_organisationtype_ck check (char_length(trim(organisation_type)) > 0),
	constraint mapping_organisationtype_name_uq unique (name),
	constraint mapping_organisationtype_name_ck check (char_length(trim(name)) > 0)
);

create table mapping.organisation
(
	ods_code varchar(10) not null,
	organisation_name varchar(100) null,
	organisation_type char(2) null,
	address_line1 varchar(100) null,
	address_line2 varchar(100) null,
	town varchar(100) null,
	county varchar(100) null,
	postcode varchar(10) null,
	phone_number varchar(20) null,
	is_mapped boolean not null,
	last_updated timestamp not null,

	constraint mapping_organisation_odscode_pk primary key (ods_code),
	constraint mapping_organisation_odscode_ck check ((char_length(trim(ods_code)) > 0) and (upper(ods_code) = ods_code)),
	constraint mapping_organisation_organisationtype_fk foreign key (organisation_type) references mapping.organisation_type (organisation_type)
);

insert into mapping.organisation_type 
(
	organisation_type, 
	name
) 
values 
('AR', 'Application Service Provider'),
('BM', 'Booking Management System (BMS) Call Centre Establishment'),
('CN', 'Cancer Network'),
('CR', 'Cancer Registry'),
('CQ', 'Care Home Headquarters'),
('CT', 'Care Trust'),
('CC', 'Clinical Commissioning Group (CCG)'),
('CL', 'Clinical Network'),
('CA', 'Commissioning Support Unit (CSU)'),
('JG', 'Court'),
('DD', 'Dental Practice'),
('ED', 'Education Establishment'),
('EA', 'Executive Agency'),
('AP', 'Executive Agency Programme'),
('GD', 'Government Department'),
('GO', 'Government Office Region (GOR)'),
('AA', 'Abeyance and Dispersal GP Practice'),
('PR', 'GP Practices in England and Wales'),
('HA', 'High Level Health Geography'),
('JD', 'Immigration Removal Centre'),
('PH', 'Independent Sector Healthcare Provider (ISHP)'),
('EL', 'Local Authority'),
('LB', 'Local Health Board (Wales)'),
('LO', 'Local Service Provider (LSP)'),
('MH', 'Military Hospital'),
('NP', 'National Application Service Provider'),
('RO', 'National Groupings'),
('NS', 'NHS Support Agency'),
('TR', 'NHS Trust'),
('TS', 'NHS Trust Service'),
('NN', 'Non-NHS Organisation'),
('NA', 'Northern Ireland Health & Social Care Board'),
('NB', 'Northern Ireland Health & Social Care Trust'),
('NC', 'Northern Ireland Local Commissioning Group'),
('OH', 'Optical Headquarters'),
('OA', 'Other Statutory Authority (OSA)'),
('PY', 'Pharmacy'),
('PX', 'Pharmacy Headquarters'),
('JE', 'Police Constabulary'),
('JF', 'Police Custody Suite'),
('PT', 'Primary Care Trust'),
('ID', 'Primary Healthcare Directorate (Isle of Man)'),
('PN', 'Prison Health Service'),
('EE', 'School'),
('JC', 'Secure Children''s Home'),
('JB', 'Secure Training Centre (STC)'),
('JH', 'Sexual Assault Referral Centre (SARC)'),
('SA', 'Special Health Authority (SpHA)'),
('WA', 'Welsh Assembly'),
('WH', 'Welsh Health Commission'),
('LH', 'Welsh Local Health Board'),
('JA', 'Young Offenders Institute');

