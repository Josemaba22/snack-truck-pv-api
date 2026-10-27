-- La nota deja de ser de la orden y pasa a cada snack (order_details.notes).
-- Las notas de órdenes existentes se conservan en el primer snack de cada orden.
ALTER TABLE order_details ADD COLUMN IF NOT EXISTS notes TEXT;

UPDATE order_details d
SET notes = o.notes
FROM orders o
WHERE d.order_id = o.id
  AND o.notes IS NOT NULL
  AND d.id = (
      SELECT d2.id FROM order_details d2
      WHERE d2.order_id = o.id
      ORDER BY d2.id
      LIMIT 1
  );

ALTER TABLE orders DROP COLUMN IF EXISTS notes;
