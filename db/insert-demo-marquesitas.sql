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

INSERT INTO ingredients (name, price, available)
VALUES
('Tortilla',0,TRUE),
('Fresa',15,TRUE),
('Platano',15,TRUE),
('Nutella',20,TRUE),
('Lechera',15,TRUE),
('Cajeta',15,TRUE),
('Oreo',15,TRUE);

-- Receta base de la Marquefresa
INSERT INTO product_recipe_details (product_id, ingredient_id, is_base)
SELECT p.id, i.id, TRUE
FROM products p, ingredients i
WHERE p.name='Marquefresa'
AND i.name='Tortilla';

-- Extras disponibles para la Marquefresa
INSERT INTO product_recipe_details (product_id, ingredient_id, is_base)
SELECT p.id, i.id, FALSE
FROM products p, ingredients i
WHERE p.name='Marquefresa'
AND i.name IN ('Nutella','Lechera','Cajeta','Oreo','Fresa','Platano');


