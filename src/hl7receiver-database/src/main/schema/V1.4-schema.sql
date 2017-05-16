/*
	Schema V1.4: Add filtered index on log.message
*/

create index log_message_nextattemptdate_ix on log.message (next_attempt_date) 
where next_attempt_date is not null;
