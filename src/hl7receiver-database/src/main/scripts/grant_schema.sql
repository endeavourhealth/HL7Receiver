/*
	grant schema
*/

do
$$
declare
	_application_user varchar(100);
	_schema_name varchar(100);
begin

	_application_user = 'endeavour';

	if exists
	(
		select *
		from pg_roles
		where rolname = _application_user
	) then

		execute 'grant all on database ' || current_database() || ' to ' || _application_user;

		for _schema_name in
		(
			select
				n.nspname
			from pg_namespace n
			where n.nspname not like 'pg_%'
			and n.nspname not in
			(
				'public',
				'information_schema'
			)
		)
		loop

			execute 'grant all on schema ' || _schema_name || ' to ' || _application_user;
			execute 'grant all privileges on all tables in schema ' || _schema_name || ' to ' || _application_user;
			execute 'grant all privileges on all sequences in schema ' || _schema_name || ' to ' || _application_user;
			execute 'grant all privileges on all functions in schema ' || _schema_name || ' to ' || _application_user;

		end loop;

	end if;

end
$$ language plpgsql;
