/*
	Schema V2.3: Add index on message control ID on table log.message
*/

create index concurrently log_message_messagecontrolid_ix on log.message (message_control_id);

