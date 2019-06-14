-- we want to allow multiple MRNs to link to the same patient UUID
ALTER TABLE mapping.resource_uuid DROP CONSTRAINT mapping_resourceuuid_resourceuuid_uq;
