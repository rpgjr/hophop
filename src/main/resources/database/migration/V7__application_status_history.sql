-- Every status an application has had. The status name and color are copied in,
-- so the history stays readable after a status is renamed or recolored.
CREATE TABLE application_status_history (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  application_id  BIGINT NOT NULL,
  from_status     VARCHAR(50) NULL,             -- NULL = the application was just added
  from_color      CHAR(7) NULL,
  to_status       VARCHAR(50) NOT NULL,
  to_color        CHAR(7) NOT NULL,
  changed_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_status_history_application
    FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,

  INDEX idx_status_history_application (application_id, changed_at)
);

-- Applications that already exist start with their current status, dated when they were added.
INSERT INTO application_status_history (application_id, to_status, to_color, changed_at)
SELECT a.id, s.name, s.color, a.created_at
FROM applications a
JOIN statuses s ON s.id = a.status_id;
