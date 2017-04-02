
create or replace function helper.get_table_sizes
(
)
returns table
(
	table_name varchar,
	table_size varchar,
	indexes_size varchar,
	total_size varchar
)
as $$
begin

	return query
	select
		cast(t2.table_name as varchar),
		cast(pg_size_pretty(t2.table_size) as varchar) as table_size,
		cast(pg_size_pretty(t2.indexes_size) as varchar) as indexes_size,
		cast(pg_size_pretty(t2.total_size) as varchar) as total_size
	from
	(
		select
			t1.table_name,
			pg_table_size(t1.table_name) as table_size,
			pg_indexes_size(t1.table_name) as indexes_size,
			pg_total_relation_size(t1.table_name) as total_size
		from
		(
			select 
				t.table_schema || '.' || t.table_name as table_name
			from information_schema.tables t
			where t.table_schema not in 
			(
				'pg_catalog', 
				'information_schema'
			)
		) as t1
		order by total_size desc
	) as t2;

end;
$$ language plpgsql;
