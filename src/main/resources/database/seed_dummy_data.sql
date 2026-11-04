-- Dummy data for HOPHOP. Run by hand AFTER V1__init.sql:
--   MYSQL_PWD='<password>' mysql -uroot hophop < seed_dummy_data.sql

INSERT INTO statuses (name, color) VALUES
  ('Applied',   '#3b82f6'),
  ('Interview', '#f59e0b'),
  ('Offer',     '#22c55e'),
  ('Rejected',  '#ef4444');

INSERT INTO applications
  (status_id, title, company, salary_min, salary_max, salary_currency, employment_type, setup, remarks, link, applied_date)
VALUES
  ((SELECT id FROM statuses WHERE name = 'Applied'),   'Frontend Developer',  'Acme Corp',  60000.00, 60000.00, 'PHP', 'Full-time', 'Hybrid',  'Referred by a friend',         'https://example.com/jobs/frontend-developer',  '2026-11-03'),
  ((SELECT id FROM statuses WHERE name = 'Interview'), 'Java Developer',      'Globex',     85000.00, 110000.00, 'PHP', 'Full-time', 'WFH',     'Technical interview on Friday', 'https://example.com/jobs/java-developer',      '2026-11-03'),
  ((SELECT id FROM statuses WHERE name = 'Applied'),   'Full Stack Engineer', 'Initech',    NULL, NULL, 'PHP', 'Contractual',     'Onsite', 'Salary not listed',            'https://example.com/jobs/full-stack-engineer', '2026-11-04'),
  ((SELECT id FROM statuses WHERE name = 'Applied'),   'Backend Engineer',    'Umbrella',   95000.00, 95000.00, 'PHP', 'Full-time', 'WFH',     'Spring Boot + MySQL',          'https://example.com/jobs/backend-engineer',    '2026-11-10'),
  ((SELECT id FROM statuses WHERE name = 'Offer'),     'React Developer',     'Hooli',      3000.00, 4000.00, 'USD', 'Part-time', 'Hybrid',  NULL,                           'https://example.com/jobs/react-developer',     '2026-11-10'),
  ((SELECT id FROM statuses WHERE name = 'Rejected'),  'QA Engineer',        'Stark Tech', 55000.00, 55000.00, 'PHP', 'Contractual', 'Onsite', 'Rejected after final round',   'https://example.com/jobs/qa-engineer',         '2026-11-10');
