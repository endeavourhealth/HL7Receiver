/*
	Schema V2.0: Alter mapping.organisation table
*/

alter table mapping.organisation drop column phone_number;

alter table mapping.organisation add constraint mapping_organisation_organisationname_ck check (char_length(trim(organisation_name)) > 0);

alter table mapping.organisation add constraint mapping_organisation_allfields_ck check 
(
	(
		is_mapped 
		and organisation_name is not null
		and address_line1 is not null 
		and address_line2 is not null 
		and town is not null 
		and county is not null 
		and postcode is not null 
	)
	or
	(
		(not is_mapped)
		and organisation_name is null
		and address_line1 is null
		and address_line2 is null
		and town is null
		and county is null
		and postcode is null
	)
);
