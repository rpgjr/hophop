-- One row = one rule: "on this website, read this tag to fill this form field".
CREATE TABLE site_mappings (
                               id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                               site_host   VARCHAR(255) NOT NULL,                 -- lowercase, no www., e.g. ph.jobstreet.com
                               field_name  ENUM('TITLE', 'COMPANY', 'SALARY_MIN', 'SALARY_MAX', 'SALARY_CURRENCY',
                   'EMPLOYMENT_TYPE', 'SETUP', 'LOCATION', 'DESCRIPTION', 'REMARKS') NOT NULL,
                               source_type ENUM('META', 'JSON_LD') NOT NULL,      -- META = <meta> tag, JSON_LD = JobPosting data block
                               source_key  VARCHAR(255) NOT NULL,                 -- og:title, or hiringOrganization.name
                               created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- a website can map each form field only once
                               UNIQUE KEY uq_site_mappings_host_field (site_host, field_name)
);