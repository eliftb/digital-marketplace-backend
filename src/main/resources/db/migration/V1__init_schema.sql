-- ============================================================
-- Dijital Pazar Yeri - İlk Şema Migrasyonu
-- ============================================================

-- Kullanıcı Rolleri
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'PRODUCER', 'CONSUMER');

-- Hesap Durumu
CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'BANNED');

-- Sipariş Durumu
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED');

-- Ödeme Durumu
CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

-- Teslimat Tercihi
CREATE TYPE delivery_type AS ENUM ('PICKUP', 'SHIPPING', 'BOTH');

-- ============================================================
-- KULLANICILAR
-- ============================================================
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            user_role NOT NULL DEFAULT 'CONSUMER',
    status          account_status NOT NULL DEFAULT 'ACTIVE',
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ŞİFRE SIFIRLAMA TOKENLERİ
-- ============================================================
CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- REFRESH TOKEN
-- ============================================================
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ŞEHİRLER / BÖLGELER
-- ============================================================
CREATE TABLE cities (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    code    VARCHAR(10)
);

CREATE TABLE districts (
    id      BIGSERIAL PRIMARY KEY,
    city_id BIGINT NOT NULL REFERENCES cities(id),
    name    VARCHAR(100) NOT NULL
);

-- ============================================================
-- ÜRETİCİ PROFİLİ
-- ============================================================
CREATE TABLE producer_profiles (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    store_name          VARCHAR(200) NOT NULL,
    store_description   TEXT,
    city_id             BIGINT REFERENCES cities(id),
    district_id         BIGINT REFERENCES districts(id),
    address             TEXT,
    tax_number          VARCHAR(50),
    iban                VARCHAR(50),
    logo_url            VARCHAR(500),
    cover_url           VARCHAR(500),
    commission_rate     DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    approval_status     account_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    approved_by         BIGINT REFERENCES users(id),
    approved_at         TIMESTAMP,
    rejection_reason    TEXT,
    total_sales         DECIMAL(12,2) NOT NULL DEFAULT 0,
    rating              DECIMAL(3,2),
    rating_count        INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- KATEGORİLER
-- ============================================================
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    parent_id   BIGINT REFERENCES categories(id),
    icon_url    VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ÜRÜNLER
-- ============================================================
CREATE TABLE products (
    id                  BIGSERIAL PRIMARY KEY,
    producer_profile_id BIGINT NOT NULL REFERENCES producer_profiles(id) ON DELETE CASCADE,
    category_id         BIGINT NOT NULL REFERENCES categories(id),
    name                VARCHAR(300) NOT NULL,
    slug                VARCHAR(350) NOT NULL UNIQUE,
    description         TEXT,
    price               DECIMAL(10,2) NOT NULL,
    stock_quantity      INTEGER NOT NULL DEFAULT 0,
    unit                VARCHAR(30),          -- kg, adet, litre
    min_order_quantity  INTEGER NOT NULL DEFAULT 1,
    delivery_type       delivery_type NOT NULL DEFAULT 'BOTH',
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    featured            BOOLEAN NOT NULL DEFAULT FALSE,
    sold_count          INTEGER NOT NULL DEFAULT 0,
    rating              DECIMAL(3,2),
    rating_count        INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Ürün görselleri
CREATE TABLE product_images (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(200),
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================
-- TÜKETİCİ PROFİLİ
-- ============================================================
CREATE TABLE consumer_profiles (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    city_id     BIGINT REFERENCES cities(id),
    district_id BIGINT REFERENCES districts(id),
    address     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ADRESLER
-- ============================================================
CREATE TABLE addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(100) NOT NULL,
    full_name   VARCHAR(200) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    city_id     BIGINT NOT NULL REFERENCES cities(id),
    district_id BIGINT NOT NULL REFERENCES districts(id),
    address     TEXT NOT NULL,
    zip_code    VARCHAR(10),
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SİPARİŞLER
-- ============================================================
CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    consumer_id         BIGINT NOT NULL REFERENCES users(id),
    order_number        VARCHAR(50) NOT NULL UNIQUE,
    status              order_status NOT NULL DEFAULT 'PENDING',
    delivery_type       delivery_type NOT NULL,
    shipping_address_id BIGINT REFERENCES addresses(id),
    subtotal            DECIMAL(12,2) NOT NULL,
    commission_total    DECIMAL(12,2) NOT NULL DEFAULT 0,
    shipping_fee        DECIMAL(8,2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(12,2) NOT NULL,
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sipariş Kalemleri
CREATE TABLE order_items (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id          BIGINT NOT NULL REFERENCES products(id),
    producer_profile_id BIGINT NOT NULL REFERENCES producer_profiles(id),
    product_name        VARCHAR(300) NOT NULL,  -- snapshot
    unit_price          DECIMAL(10,2) NOT NULL,  -- snapshot
    quantity            INTEGER NOT NULL,
    commission_rate     DECIMAL(5,2) NOT NULL,   -- snapshot
    commission_amount   DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(12,2) NOT NULL,
    status              order_status NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ÖDEMELER
-- ============================================================
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id),
    amount          DECIMAL(12,2) NOT NULL,
    status          payment_status NOT NULL DEFAULT 'PENDING',
    method          VARCHAR(50),    -- CREDIT_CARD, BANK_TRANSFER, etc.
    transaction_id  VARCHAR(200),
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- DEĞERLENDİRMELER
-- ============================================================
CREATE TABLE reviews (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    consumer_id     BIGINT NOT NULL REFERENCES users(id),
    order_item_id   BIGINT REFERENCES order_items(id),
    rating          SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    approved        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (order_item_id, consumer_id)
);

-- ============================================================
-- FAVORİLER
-- ============================================================
CREATE TABLE favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, product_id)
);

-- ============================================================
-- KOMİSYON HAREKETLERİ
-- ============================================================
CREATE TABLE commission_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    order_item_id       BIGINT NOT NULL REFERENCES order_items(id),
    producer_profile_id BIGINT NOT NULL REFERENCES producer_profiles(id),
    gross_amount        DECIMAL(12,2) NOT NULL,
    commission_rate     DECIMAL(5,2) NOT NULL,
    commission_amount   DECIMAL(12,2) NOT NULL,
    net_amount          DECIMAL(12,2) NOT NULL,
    paid_to_producer    BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PLATFORM AYARLARI
-- ============================================================
CREATE TABLE platform_settings (
    id          BIGSERIAL PRIMARY KEY,
    key         VARCHAR(100) NOT NULL UNIQUE,
    value       TEXT NOT NULL,
    description TEXT,
    updated_by  BIGINT REFERENCES users(id),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- İNDEKSLER
-- ============================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_products_producer ON products(producer_profile_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_orders_consumer ON orders(consumer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_producer ON order_items(producer_profile_id);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_commission_producer ON commission_transactions(producer_profile_id);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Platform Ayarları
INSERT INTO platform_settings (key, value, description) VALUES
('default_commission_rate', '10.00', 'Varsayılan komisyon oranı (%)'),
('min_commission_rate', '5.00', 'Minimum komisyon oranı (%)'),
('max_commission_rate', '30.00', 'Maksimum komisyon oranı (%)'),
('shipping_fee', '29.90', 'Standart kargo ücreti (TL)'),
('free_shipping_threshold', '500.00', 'Ücretsiz kargo sınırı (TL)');

-- Kategoriler
INSERT INTO categories (name, slug, description, sort_order) VALUES
('Tarım Ürünleri', 'tarim-urunleri', 'Taze sebze, meyve ve tahıllar', 1),
('Süt ve Süt Ürünleri', 'sut-urunleri', 'Peynir, yoğurt, tereyağı vb.', 2),
('El Sanatları', 'el-sanatlari', 'El yapımı ürünler ve zanaat eserleri', 3),
('Ev Yapımı Gıda', 'ev-yapimi-gida', 'Ev yapımı yiyecek ve içecekler', 4),
('Organik Ürünler', 'organik-urunler', 'Sertifikalı organik ürünler', 5),
('Tekstil ve Giyim', 'tekstil-giyim', 'El dokuma ve geleneksel tekstil', 6);

-- Örnek şehirler
INSERT INTO cities (name, code) VALUES
('İstanbul', '34'), ('Ankara', '06'), ('İzmir', '35'),
('Bursa', '16'), ('Antalya', '07'), ('Adana', '01'),
('Konya', '42'), ('Gaziantep', '27'), ('Mersin', '33'), ('Kayseri', '38');

-- Süper Admin kullanıcısı (şifre: Admin123!)
INSERT INTO users (email, password_hash, first_name, last_name, role, status, email_verified)
VALUES ('admin@pazaryeri.com',
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- bcrypt of 'Admin123!'
        'Platform', 'Yöneticisi', 'SUPER_ADMIN', 'ACTIVE', TRUE);
