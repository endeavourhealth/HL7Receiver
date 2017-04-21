
create or replace function helper.get_db_connections
(
)
returns table
(
	database_name varchar,
	username varchar,
	connection_count integer
)
as $$
begin

	return query
	select
		cast(c.database_name as varchar),
		cast(c.username as varchar),
		cast(c.connection_count as integer)
	from
	(
		select
			1 as listing_type,
			datname as database_name, 
			usename as username, 
			count(*) as connection_count 
		from pg_stat_activity 
		group by datname, usename 
		union all
		select
			2 as listing_type,
			'TOTAL' as database_name,
			'--------' as username,
			count(*) as connection_count
		from pg_stat_activity
	) c
	order by listing_type asc, connection_count desc;

end;
$$ language plpgsql;
