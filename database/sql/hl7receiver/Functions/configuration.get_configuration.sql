
create or replace function configuration.get_configuration
(
	_hostname varchar(100)
)
returns setof refcursor
as $$
declare
	_instance_id integer;
	log_instance refcursor;
	configuration_channel refcursor;
	configuration_channel_message_type refcursor;
	configuration_eds refcursor;
	configuration_notification_attempt_interval refcursor;
begin

	insert into log.instance
	(
		instance_id,
		hostname,
		added_date,
		last_get_config_date
	)
	select
		coalesce(max(instance_id), 0) + 1 as instance_id,
		_hostname,
		now(),
		now()
	from log.instance
	on conflict (hostname) do 
	update
	set last_get_config_date = now()
	where instance.hostname = _hostname
	returning instance_id into _instance_id;

	------------------------------------------------------
	log_instance = 'log_instance';

	open log_instance for
	select _instance_id as instance_id;
	
	return next log_instance;
	
	------------------------------------------------------
	configuration_channel = 'configuration_channel';

	open configuration_channel for
	select 
		c.channel_id,
		c.channel_name,
		c.port_number,
		c.is_active,
		c.use_tls,
		c.sending_application,
		c.sending_facility,
		c.receiving_application,
		c.receiving_facility,
		c.pid1_field,
		c.pid1_assigning_auth,
		c.pid2_field,
		c.pid2_assigning_auth,
		c.eds_service_identifier,
		c.notes
	from configuration.channel c;
	
	return next configuration_channel;
	
	------------------------------------------------------
	configuration_channel_message_type = 'configuration_channel_message_type';

	open configuration_channel_message_type for
	select
		t.channel_id,
		t.message_type,
		t.is_allowed		
	from configuration.channel_message_type t;
	
	return next configuration_channel_message_type;

	------------------------------------------------------
	configuration_eds = 'configuration_eds';
	
	open configuration_eds for
	select
		e.eds_url,
		e.software_content_type,
		e.software_version,
		e.use_keycloak,
		e.keycloak_token_uri,
		e.keycloak_realm,
		e.keycloak_username,
		e.keycloak_password,
		e.keycloak_clientid
	from configuration.eds e;
	
	return next configuration_eds;
	
	------------------------------------------------------
	configuration_notification_attempt_interval = 'configuration_notification_attempt_interval';
	
	open configuration_notification_attempt_interval for
	select
		interval_seconds
	from configuration.notification_attempt_interval
	order by interval_seconds asc;
	
	return next configuration_notification_attempt_interval;
	
	------------------------------------------------------
	
end;
$$ language plpgsql;
