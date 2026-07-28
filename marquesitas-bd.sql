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
-- PRODUCTS ADDONS
-- ============================================

CREATE TABLE products_addons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    available BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- PRODUCT DETAILS
-- Relación Producto <-> Addon
-- ============================================

CREATE TABLE product_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL,

    addon_id UUID NOT NULL,

    price_override NUMERIC(10,2),

    available BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_product_detail_product
        FOREIGN KEY(product_id)
        REFERENCES products(id),

    CONSTRAINT fk_product_detail_addon
        FOREIGN KEY(addon_id)
        REFERENCES products_addons(id),

    CONSTRAINT uq_product_addon
        UNIQUE(product_id, addon_id)
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

    selected_addons JSONB,

    subtotal NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_order_detail_order
        FOREIGN KEY(order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_detail_product
        FOREIGN KEY(product_id)
        REFERENCES products(id)
);

CREATE INDEX idx_products_category
ON products(category_id);

CREATE INDEX idx_product_details_product
ON product_details(product_id);

CREATE INDEX idx_product_details_addon
ON product_details(addon_id);

CREATE INDEX idx_order_details_order
ON order_details(order_id);

CREATE INDEX idx_order_details_product
ON order_details(product_id);

CREATE INDEX idx_orders_status
ON orders(status);

CREATE INDEX idx_orders_created_at
ON orders(created_at);