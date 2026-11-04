-- HOPHOP schema (no users, no login). Flyway runs this once, on first start.
-- To run it by hand instead, see SETUP.md.

CREATE TABLE statuses (
  id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(50) NOT NULL UNIQUE,
  color CHAR(7) NOT NULL                -- hex color, e.g. #3b82f6
);

CREATE TABLE applications (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  status_id       BIGINT NOT NULL,

  title           VARCHAR(255) NOT NULL,
  company         VARCHAR(255) NOT NULL,
  salary_min      DECIMAL(12,2) NULL,               -- NULL = not listed
  salary_max      DECIMAL(12,2) NULL,               -- NULL or = salary_min for a single amount
  salary_currency ENUM('PHP', 'USD') NOT NULL DEFAULT 'PHP',
  setup           ENUM('Onsite', 'Hybrid', 'WFH') NOT NULL DEFAULT 'Onsite',
  remarks         TEXT NULL,
  link            VARCHAR(2048) NOT NULL,
  applied_date    DATE NOT NULL,                    -- the date the calendar uses

  -- extra details a link scraper can fill in later
  location        VARCHAR(255) NULL,
  employment_type ENUM('Full-time', 'Part-time', 'Contractual') NULL,
  source_site     VARCHAR(100) NULL,
  description     TEXT NULL,

  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- lets MySQL reject the same posting saved twice
  link_hash       CHAR(64) AS (SHA2(link, 256)) STORED,

  CONSTRAINT chk_applications_salary_range
    CHECK (salary_max IS NULL OR salary_min IS NOT NULL AND salary_max >= salary_min),
  CONSTRAINT fk_applications_status
    FOREIGN KEY (status_id) REFERENCES statuses (id) ON DELETE RESTRICT,

  UNIQUE KEY uq_applications_link (link_hash),
  INDEX idx_applications_date (applied_date)
);
