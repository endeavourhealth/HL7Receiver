/* 
	Schema V2.5: Change nullability on table mapping.organisation column organisation_type
*/

alter table mapping.organisation alter column organisation_type drop not null;

alter table mapping.organisation drop constraint mapping_organisation_allfields_ck;

alter table mapping.organisation add constraint mapping_organisation_allfields_ck check
(
	(
		is_mapped 
		and organisation_name is not null
		and organisation_class is not null
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
);
