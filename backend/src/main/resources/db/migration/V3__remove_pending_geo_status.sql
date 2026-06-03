ALTER TABLE click_analytics ALTER COLUMN geo_status SET DEFAULT 'DISABLED';
UPDATE click_analytics SET geo_status = 'DISABLED' WHERE geo_status = 'PENDING';
