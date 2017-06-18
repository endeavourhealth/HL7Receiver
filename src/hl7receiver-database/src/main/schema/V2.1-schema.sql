/*
	Schema V2.1: Alter mapping.organisation table
*/

create table mapping.organisation_class
(
	organisation_class char(1) not null,
	organisation_class_name varchar(10) not null,
	description varchar(100) not null,
	
	constraint mapping_organisationclass_organisationclass_pk primary key (organisation_class),
	constraint mapping_organisationclass_organisationclass_ck check (organisation_class != ' '),
	constraint mapping_organisationclass_organisationclassname_uq unique (organisation_class_name),
	constraint mapping_organisationclass_organisationclassname_ck check (char_length(trim(organisation_class_name)) > 0)
);

insert into mapping.organisation_class (organisation_class, organisation_class_name, description) values
('O', 'HSCOrg', 'http://www.datadictionary.nhs.uk/data_dictionary/nhs_business_definitions/o/organisation_de.asp'),
('S', 'HSCSite', 'http://www.datadictionary.nhs.uk/data_dictionary/nhs_business_definitions/o/organisation_site_de.asp');

alter table mapping.organisation drop constraint mapping_organisation_allfields_ck;
alter table mapping.organisation drop constraint mapping_organisation_organisationtype_fk;
alter table mapping.organisation drop constraint mapping_organisation_organisationname_ck;
alter table mapping.organisation drop constraint mapping_organisation_odscode_ck;
alter table mapping.organisation drop constraint mapping_organisation_odscode_pk;

alter table mapping.organisation rename to organisation_old;

create table mapping.organisation
(
	ods_code varchar(10) not null,
	organisation_name varchar(100) null,
	organisation_class char(1) null,
	organisation_type char(2) null,
	address_line1 varchar(100) null,
	address_line2 varchar(100) null,
	town varchar(100) null,
	county varchar(100) null,
	postcode varchar(10) null,
	is_mapped boolean not null,
	manual_mapping boolean null,
	last_updated timestamp not null,

	constraint mapping_organisation_odscode_pk primary key (ods_code),
	constraint mapping_organisation_odscode_ck check ((char_length(trim(ods_code)) > 0) and (upper(ods_code) = ods_code)),
	constraint mapping_organisation_organisationname_ck check (char_length(trim(organisation_name)) > 0),
	constraint mapping_organisation_organisationclass_fk foreign key (organisation_class) references mapping.organisation_class (organisation_class),
	constraint mapping_organisation_organisationtype_fk foreign key (organisation_type) references mapping.organisation_type (organisation_type),
	constraint mapping_organisation_allfields_ck check 
	(
		(
			is_mapped 
			and organisation_name is not null
			and organisation_class is not null
			and organisation_type is not null 
			and address_line1 is not null 
			and address_line2 is not null 
			and town is not null 
			and county is not null 
			and postcode is not null
			and manual_mapping is not null
		)
		or
		(
			(not is_mapped)
			and organisation_name is null
			and organisation_class is null
			and organisation_type is null
			and address_line1 is null
			and address_line2 is null
			and town is null
			and county is null
			and postcode is null
			and manual_mapping is null
		)
	)
);

insert into mapping.organisation 
(
	ods_code, 
	organisation_name, 
	organisation_class, 
	organisation_type, 
	address_line1, 
	address_line2, 
	town, 
	county, 
	postcode, 
	is_mapped, 
	manual_mapping,
	last_updated
)
select
	ods_code,
	organisation_name,
	'O',
	organisation_type,
	address_line1,
	address_line2,
	town,
	county,
	postcode,
	is_mapped,
	false,
	last_updated
from mapping.organisation	 _old;

drop table mapping.organisation_old;
