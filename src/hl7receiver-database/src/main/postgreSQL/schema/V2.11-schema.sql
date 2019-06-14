-- index to improve speed of the get_similar_resource_uuid_mappings(..) function
create index ix_resource_uuid_unique_identifier_scope_id  on mapping.resource_uuid (unique_identifier, scope_id);