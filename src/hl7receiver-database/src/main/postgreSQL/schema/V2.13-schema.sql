CREATE TABLE log.last_message
(
    message_id integer NOT NULL,
    channel_id integer NOT NULL,
    log_date timestamp without time zone NOT NULL,
    constraint pk_last_message primary key (channel_id)
);