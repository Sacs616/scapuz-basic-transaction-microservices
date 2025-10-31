-- Change genre column from VARCHAR to CHAR
ALTER TABLE clients 
ALTER COLUMN genre TYPE CHAR(1);

-- Update the check constraint to maintain data integrity
ALTER TABLE clients 
DROP CONSTRAINT IF EXISTS chk_genre;

ALTER TABLE clients 
ADD CONSTRAINT chk_genre CHECK (genre IN ('M', 'F', 'O'));

-- Optional: Add comment to the column
COMMENT ON COLUMN clients.genre IS 'Gender stored as single character: M-Male, F-Female, O-Other';