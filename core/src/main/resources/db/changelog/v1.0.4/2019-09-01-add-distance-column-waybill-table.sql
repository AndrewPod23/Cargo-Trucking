ALTER TABLE waybill ADD COLUMN IF NOT EXISTS distance INTEGER NOT NULL DEFAULT 100;
ALTER TABLE waybill ALTER COLUMN distance DROP DEFAULT;