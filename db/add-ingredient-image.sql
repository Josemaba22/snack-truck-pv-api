-- Imagen opcional por ingrediente (misma convención que products.image_key).
ALTER TABLE ingredients ADD COLUMN IF NOT EXISTS image_key VARCHAR(255);
