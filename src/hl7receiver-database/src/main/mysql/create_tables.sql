
use hl7_receiver;


-- NOTE: the below MySQL script only creates a single MySQL table, which is enough to allow
-- us to run the Barts transform (which dips into the hl7 receiver DB)

CREATE TABLE resource_uuid
(
	scope_id char(1),
    resource_type varchar(50),
    unique_identifier varchar(200),
    resource_uuid char(36),
    CONSTRAINT pk_resource_uuid PRIMARY KEY (scope_id, resource_type, unique_identifier)
);



