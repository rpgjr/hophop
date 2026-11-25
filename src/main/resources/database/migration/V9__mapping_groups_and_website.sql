-- A website can now have several named mappings, each for one country.
-- Every existing website becomes one mapping named after its host.
CREATE TABLE mapping_groups (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,                  -- shown as the website pill, e.g. Jobstreet PH
  site_host   VARCHAR(255) NOT NULL,                  -- lowercase, no www., e.g. ph.jobstreet.com
  country     VARCHAR(100) NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uq_mapping_groups_name (name),
  INDEX idx_mapping_groups_host (site_host)
);

INSERT INTO mapping_groups (name, site_host)
SELECT DISTINCT site_host, site_host FROM site_mappings;

ALTER TABLE site_mappings ADD COLUMN group_id BIGINT NULL;

UPDATE site_mappings m JOIN mapping_groups g ON g.site_host = m.site_host SET m.group_id = g.id;

ALTER TABLE site_mappings DROP INDEX uq_site_mappings_host_field;

ALTER TABLE site_mappings MODIFY group_id BIGINT NOT NULL;

ALTER TABLE site_mappings ADD UNIQUE KEY uq_site_mappings_group_field (group_id, field_name);

ALTER TABLE site_mappings DROP COLUMN site_host;

-- The website an application was made on: the name of the mapping used.
ALTER TABLE applications ADD COLUMN website VARCHAR(100) NULL;

UPDATE applications a JOIN mapping_groups g
  ON a.link LIKE CONCAT('%://', g.site_host, '/%') OR a.link LIKE CONCAT('%://www.', g.site_host, '/%')
SET a.website = g.name;
