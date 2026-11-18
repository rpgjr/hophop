-- Statuses can be put in any order. Existing ones keep their current order.
ALTER TABLE statuses
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0;

UPDATE statuses SET sort_order = id;
