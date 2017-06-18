
create or replace function mapping.set_organisation
(
	_ods_code varchar(10),
	_organisation_name varchar(100),
	_organisation_class char(1),
	_organisation_type char(2),
	_address_line1 varchar(100),
	_address_line2 varchar(100),
	_town varchar(100),
	_county varchar(100),
	_postcode varchar(10),
	_manual_mapping boolean
)
returns void
as $$
begin

	_ods_code = trim(upper(_ods_code));
	
	if (char_length(_ods_code) = 0) 
	then
		raise exception '_ods_code is empty';
		return;
	end if;
	
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
	values
	(
		_ods_code,
		_organisation_name,
		_organisation_class,
		_organisation_type,
		coalesce(_address_line1, ''),
		coalesce(_address_line2, ''),
		coalesce(_town, ''),
		coalesce(_county, ''),
		coalesce(_postcode, ''),
		true,
		_manual_mapping,
		now()
	)
	on conflict (ods_code)
	do update
	set
		organisation_name = _organisation_name,
		organisation_class = _organisation_class,
		organisation_type = _organisation_type,
		address_line1 = coalesce(_address_line1, ''),
		address_line2 = coalesce(_address_line2, ''),
		town = coalesce(_town, ''),
		county = coalesce(_county, ''),
		postcode = coalesce(_postcode, ''),
		is_mapped = true,
		manual_mapping = _manual_mapping,
		last_updated = now()
	where excluded.ods_code = _ods_code;
	 	
end;
$$ language plpgsql;

