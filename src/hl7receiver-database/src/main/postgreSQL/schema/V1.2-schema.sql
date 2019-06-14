/*
	Schema V1.2: Add is_complete and error_message columns
*/

-- add is_complete
alter table log.message add is_complete boolean null;

update log.message m
set is_complete = ms.is_complete
from dictionary.message_status ms
where m.message_status_id = ms.message_status_id;

alter table log.message alter is_complete set not null;
alter table log.message add constraint log_messagestatus_messagestatusid_iscomplete_fk foreign key (message_status_id, is_complete) references dictionary.message_status (message_status_id, is_complete);

-- add error_message
alter table log.message add error_message text null;

update log.message m
set error_message = msh.error_message
from log.message_status_history msh
where m.message_id = msh.message_id and m.message_status_date = msh.message_status_date;

alter table log.message add constraint log_messagestatus_iscomplete_errormessage_ck check ((is_complete and error_message is null) or (not is_complete));

-- move processing_attempt_id column
alter table log.message drop constraint log_message_processingattemptid_ck;
alter table log.message rename processing_attempt_id to processing_attempt_id_old;
alter table log.message add processing_attempt_id smallint null;

update log.message
set processing_attempt_id = processing_attempt_id_old;

alter table log.message alter processing_attempt_id set not null;
alter table log.message add constraint log_message_processingattemptid_ck check (processing_attempt_id >= 0);
alter table log.message drop processing_attempt_id_old;

-- move next_attempt_date column
alter table log.message rename next_attempt_date to next_attempt_date_old;
alter table log.message add next_attempt_date timestamp null;

update log.message 
set next_attempt_date = next_attempt_date_old;

alter table log.message drop next_attempt_date_old;
