/* 
	drop database 
*/

select pg_terminate_backend(pg_stat_activity.pid)
from pg_stat_activity
where pg_stat_activity.datname = 'hl7receiver'
and pid <> pg_backend_pid();

drop database hl7receiver;

