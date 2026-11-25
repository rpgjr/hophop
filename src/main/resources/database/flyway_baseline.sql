-- Run this ONCE by hand on a database that was set up before Flyway (the TiDB one).
-- Flyway cannot create its own history table there: TiDB refuses the CREATE TABLE ... SELECT it uses to baseline.
-- This makes the table and marks every file up to VERSION as already applied. Flyway then runs only newer files.
--
-- Change '9' in the INSERT to the last file you ran by hand. Check with:
--   SHOW COLUMNS FROM applications LIKE 'website';   one row = V9 is done, no row = last done is V7 (if V8 is not done either)
-- This file is outside migration/, so Flyway never runs it.

CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
);

INSERT INTO `flyway_schema_history`
  (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `execution_time`, `success`)
VALUES
  (1, '9', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'manual', 0, 1);
