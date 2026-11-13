-- A rule can now read a visible line recognised by its look (salary, setup, employment type).
ALTER TABLE site_mappings
    MODIFY source_type ENUM('META', 'JSON_LD', 'CSS', 'PATTERN') NOT NULL;
