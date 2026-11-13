-- A rule can now read the text of a page element, for sites that only show job details in the page itself.
ALTER TABLE site_mappings
    MODIFY source_type ENUM('META', 'JSON_LD', 'CSS') NOT NULL;
