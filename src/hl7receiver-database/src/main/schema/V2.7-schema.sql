/* 
	Schema V2.7: Move dictionary.processing_content_type table into the log schema
*/

create table log.processing_content_type
(
	processing_content_type_id smallint not null,
	description varchar(100) not null,
	
	constraint log_processingcontenttype_processingcontenttypeid_pk primary key (processing_content_type_id),
	constraint log_processingcontenttype_description_uq unique (description),
	constraint log_processingcontenttype_description_ck check (char_length(trim(description)) > 0)
);

insert into log.processing_content_type
(
	processing_content_type_id,
	description
)
select
	processing_content_type_id,
	description
from dictionary.processing_content_type;

alter table log.message_processing_content drop constraint log_messageprocessingcontent_processingcontenttypeid_fk;
alter table log.message_processing_content add constraint log_messageprocessingcontent_processingcontenttypeid_fk foreign key (processing_content_type_id) references log.processing_content_type (processing_content_type_id);


drop table dictionary.processing_content_type;
