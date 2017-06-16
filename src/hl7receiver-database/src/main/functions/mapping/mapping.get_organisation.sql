
create or replace function mapping.get_organisation
(
	_ods_code varchar(10)
)
returns table
(
	ods_code varchar(10),
	organisation_name varchar(100),
	organisation_type char(2),
	address_line1 varchar(100),
	address_line2 varchar(100),
	town varchar(100),
	county varchar(100),
	postcode varchar(10),
	phone_number varchar(20),
	is_mapped boolean
)
as $$
begin

	_ods_code = trim(upper(_ods_code));
	
	if (char_length(_ods_code) = 0) 
	then
		raise exception '_ods_code is empty';
		return;
	end if;
	
	if not exists
	(
		select *
		from mapping.organisation o
		where o.ods_code = _ods_code
	)
	then
		insert into mapping.organisation
		(
			ods_code,
			organisation_name,
			organisation_type,
			address_line1,
			address_line2,
			town,
			county,
			postcode,
			phone_number,
			is_mapped,
			last_updated
			
		)
		values
		(
			_ods_code,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			false,
			now()
		);
	end if;
	
	return query
	select
		o.ods_code,
		o.organisation_name,
		o.organisation_type,
		o.address_line1,
		o.address_line2,
		o.town,
		o.county,
		o.postcode,
		o.phone_number,
		o.is_mapped
	from mapping.organisation o
	where o.ods_code = _ods_code;
 	
end;
$$ language plpgsql;

