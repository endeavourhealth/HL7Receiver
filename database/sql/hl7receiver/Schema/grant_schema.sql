/* 
	grant schema 
*/

grant all on database hl7receiver TO postgres;

grant all on schema configuration TO postgres;
grant all privileges on all tables in schema configuration to postgres;
grant all privileges on all sequences in schema configuration to postgres;
grant all privileges on all functions in schema configuration to postgres;

grant all on schema log TO postgres;
grant all privileges on all tables in schema log to postgres;
grant all privileges on all sequences in schema log to postgres;
grant all privileges on all functions in schema log to postgres;

grant all on schema dictionary TO postgres;
grant all privileges on all tables in schema dictionary to postgres;
grant all privileges on all sequences in schema dictionary to postgres;
grant all privileges on all functions in schema dictionary to postgres;

grant all on schema helper TO postgres;
grant all privileges on all tables in schema helper to postgres;
grant all privileges on all sequences in schema helper to postgres;
grant all privileges on all functions in schema helper to postgres;
