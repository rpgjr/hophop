-- The job description now comes with the application, filled from the website's mapping.
ALTER TABLE applications
    ADD COLUMN description TEXT NULL AFTER remarks;
