-- ============================================
-- EXTENSION
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- CATEGORIES
-- ============================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0
);

-- ============================================
-- PRODUCTS
-- ============================================

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    category_id UUID NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);

-- ============================================
-- INGREDIENTS
-- ============================================

CREATE TABLE ingredients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    available BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- PRODUCT RECIPE DETAILS
-- Receta de un producto: qué ingredientes lo componen
-- (is_base = TRUE) y cuáles se pueden agregar como extra
-- (is_base = FALSE)
-- ============================================

CREATE TABLE product_recipe_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL,

    ingredient_id UUID NOT NULL,

    is_base BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_product_recipe_detail_product
        FOREIGN KEY(product_id)
        REFERENCES products(id),

    CONSTRAINT fk_product_recipe_detail_ingredient
        FOREIGN KEY(ingredient_id)
        REFERENCES ingredients(id),

    CONSTRAINT uq_product_ingredient
        UNIQUE(product_id, ingredient_id)
);

-- ============================================
-- ORDERS
-- ============================================

CREATE TABLE orders (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_number BIGSERIAL UNIQUE,

    status VARCHAR(20) NOT NULL,

    subtotal NUMERIC(10,2) NOT NULL DEFAULT 0,

    total NUMERIC(10,2) NOT NULL DEFAULT 0,

    notes TEXT,

    payment_method VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    completed_at TIMESTAMP WITHOUT TIME ZONE
);

-- ============================================
-- ORDER DETAILS
-- ============================================

CREATE TABLE order_details (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_id UUID NOT NULL,

    product_id UUID NOT NULL,

    quantity INTEGER NOT NULL CHECK(quantity > 0),

    unit_price NUMERIC(10,2) NOT NULL,

    subtotal NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_order_detail_order
        FOREIGN KEY(order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_detail_product
        FOREIGN KEY(product_id)
        REFERENCES products(id)
);

-- ============================================
-- ORDER DETAIL INGREDIENTS
-- Personalización de una línea de orden respecto a la
-- receta base del producto: ingredientes agregados (ADDED)
-- o quitados (REMOVED). Solo se registran las diferencias.
-- ============================================

CREATE TABLE order_detail_ingredients (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_detail_id UUID NOT NULL,

    ingredient_id UUID NOT NULL,

    ingredient_name VARCHAR(100) NOT NULL,

    unit_price NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),

    action VARCHAR(10) NOT NULL CHECK (action IN ('ADDED', 'REMOVED')),

    CONSTRAINT fk_order_detail_ingredients_order_detail
        FOREIGN KEY (order_detail_id)
        REFERENCES order_details(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_detail_ingredients_ingredient
        FOREIGN KEY (ingredient_id)
        REFERENCES ingredients(id)
);

CREATE INDEX idx_products_category
ON products(category_id);

CREATE INDEX idx_product_recipe_details_product
ON product_recipe_details(product_id);

CREATE INDEX idx_product_recipe_details_ingredient
ON product_recipe_details(ingredient_id);

CREATE INDEX idx_order_details_order
ON order_details(order_id);

CREATE INDEX idx_order_details_product
ON order_details(product_id);

CREATE INDEX idx_orders_status
ON orders(status);

CREATE INDEX idx_orders_created_at
ON orders(created_at);

CREATE INDEX idx_order_detail_ingredients_order_detail
ON order_detail_ingredients(order_detail_id);

CREATE INDEX idx_order_detail_ingredients_ingredient
ON order_detail_ingredients(ingredient_id);

