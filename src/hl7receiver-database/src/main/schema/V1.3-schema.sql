/*
	Schema V1.3: Add filtered index on log.message
*/

create index concurrently log_message_errormessage_ix on log.message (error_message)
where error_message is not null;
