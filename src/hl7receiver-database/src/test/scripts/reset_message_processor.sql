/*
	Script to reset all *processed* message content
	Retains received messages as they are but resets the message processor to start processing messages from the start
	Also retains existing code and id mapping data
*/
-- 1 - remove existing messages from the queue (they will be re-inserted at step 4)
delete from log.message_queue

-- 2 - clear out the transformed message store
truncate table log.message_processing_content

-- 3 - clear down message status histories except for the initial receive status
delete from log.message_status_history where message_status_id != 0

-- 4 - reset all the message statuses back to 0.  this will re-insert everything back into the message queue.  this step will be slow.  e.g. 1 minute per 100,000 messages.
update log.message set message_status_id = 0, message_status_date = log_date, processing_attempt_id = 0, next_attempt_date = null

-- 5 - only if the system is offline, perform a vacuum full - this will disk space used by the above deletions
vacuum full
