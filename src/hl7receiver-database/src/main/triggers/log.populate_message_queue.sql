
create or replace function log.populate_message_queue() returns trigger 
as $$
begin

	if ((new.message_id is null) 
	or exists
	(
		select *
		from dictionary.message_status s
		where s.message_status_id = new.message_status_id
		and s.is_complete
	))
	then
		delete
		from log.message_queue mq
		where mq.message_id = new.message_id;
	else 
		insert into log.message_queue
		(
			message_id,
			channel_id,
			message_date,
			log_date
		)
		values
		(
			new.message_id,
			new.channel_id,
			new.message_date,
			new.log_date
		)
		on conflict on constraint log_messagequeue_messageid_pk
		do update
		set
			message_date = new.message_date,
			log_date = new.log_date
		where log.message_queue.message_id = new.message_id;
	end if;	
	
	return new;
end;
$$ language plpgsql;

drop trigger if exists populate_message_queue_tr on log.message;

create trigger populate_message_queue_tr after insert or update or delete on log.message
for each row execute procedure log.populate_message_queue();