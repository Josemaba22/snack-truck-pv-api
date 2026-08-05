-- ----------------------
--	INSERTS MARQUESITAS
-- ----------------------
INSERT INTO categories (name, description, display_order) VALUES
('Marquesitas', 'Marquesitas personalizadas', 1),
('Bebidas', 'Bebidas liquidas de sabor', 2),
('Postres', 'Postres preparados', 3);

INSERT INTO products (name, description, price, category_id, available)
VALUES
(
    'Marquefresa',
    'Marquesita con fresas, nutella y espuma de vainilla',
    65.00,
    (SELECT id FROM categories WHERE name='Marquesitas'),
    TRUE
),
(
    'Agua natural',
    'Agua natural',
    20.00,
    (SELECT id FROM categories WHERE name='Bebidas'),
    TRUE
),
(
    'Flan',
    'Flan de vainilla',
    45.00,
    (SELECT id FROM categories WHERE name='Postres'),
    TRUE
);

INSERT INTO products_addons (name, price, available)
VALUES
('Fresa',15,TRUE),
('Platano',15,TRUE),
('Nutella',20,TRUE),
('Lechera',15,TRUE),
('Cajeta',15,TRUE),
('Oreo',15,TRUE);

INSERT INTO product_details (product_id, addon_id)
SELECT p.id,a.id
FROM products p, products_addons a
WHERE p.name='Marquefresa'
AND a.name IN ('Nutella','Lechera','Cajeta','Oreo','Fresa','Platano');

INSERT INTO orders
(
    status,
    subtotal,
    total,
    payment_method,
    notes
)
VALUES
(
    'PENDING',
    140,
    140,
    'CASH',
    'Sin nutella'
);

INSERT INTO order_details
(
    order_id,
    product_id,
    quantity,
    unit_price,
    subtotal
)
VALUES
(
    (SELECT id FROM orders ORDER BY created_at DESC LIMIT 1),
    (SELECT id FROM products WHERE name='Marquefresa'),
    1,
    85,
    120
);

INSERT INTO order_detail_addons
(
    order_detail_id,
    addon_id,
    addon_name,
    unit_price
)
VALUES
(
    (SELECT id FROM order_details ORDER BY id DESC LIMIT 1),
    (SELECT id FROM products_addons WHERE name='Queso Extra'),
    'Queso Extra',
    15
),
(
    (SELECT id FROM order_details ORDER BY id DESC LIMIT 1),
    (SELECT id FROM products_addons WHERE name='Tocino'),
    'Tocino',
    20
);

SELECT * FROM products;