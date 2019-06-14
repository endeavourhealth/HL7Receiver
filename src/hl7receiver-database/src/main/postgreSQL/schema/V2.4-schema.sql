/* 
	Schema V2.4: Add SlackUrl channel option
*/

insert into dictionary.channel_option_type
(
	channel_option_type,
	default_value,
	description
)
values
(
	'SlackUrl', 
	'', 
	'Slack URL to post notification messages to'
);
