-- More salary currencies, for jobs outside the Philippines (MYR shows as RM, SGD as S$).
-- The location column already exists from V1; the app now reads and writes it.
ALTER TABLE applications
  MODIFY salary_currency ENUM('PHP', 'USD', 'MYR', 'SGD', 'EUR', 'GBP', 'AUD', 'JPY') NOT NULL DEFAULT 'PHP';
