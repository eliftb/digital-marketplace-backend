# Dijital Pazar Yeri - Backend

> Yerel Üretici Platformu | Spring Boot 3 + PostgreSQL + JWT

## Proje Hakkında

Yerel üreticilerin (çiftçiler, zanaatkarlar, ev yapımı ürün satıcıları) ürünlerini doğrudan tüketicilere satabildikleri bir dijital pazar yeri platformunun backend API'si.

---

## Teknoloji Stack

| Katman | Teknoloji |
|--------|-----------|
| Framework | Spring Boot 3.2 |
| Dil | Java 17 |
| Güvenlik | Spring Security + JWT (JJWT) |
| Veritabanı | PostgreSQL 15+ |
| ORM | Spring Data JPA + Hibernate |
| Migration | Flyway |
| Dokümantasyon | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Kullanıcı Rolleri

| Rol | Yetki |
|-----|-------|
| `SUPER_ADMIN` | Tüm sistem yönetimi, komisyon ayarları, platform konfigürasyonu |
| `ADMIN` | Üretici onayı/reddi, kullanıcı yönetimi |
| `PRODUCER` | Mağaza ve ürün yönetimi, sipariş takibi |
| `CONSUMER` | Ürün arama, sipariş verme, değerlendirme |

---

## Kurulum

### Gereksinimler
- Java 17+
- Maven 3.8+
- PostgreSQL 15+

### 1. Veritabanı oluştur
```sql
CREATE DATABASE pazaryeri_db;
CREATE USER pazaryeri_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE pazaryeri_db TO pazaryeri_user;
```

### 2. Ortam değişkenleri
```bash
export DB_USERNAME=pazaryeri_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_256_bit_secret_key_here_minimum_32_chars
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password
```

### 3. Çalıştır
```bash
mvn clean install
mvn spring-boot:run
```

### Docker ile çalıştır
```bash
docker-compose up -d
```

---

## API Dokümantasyonu

Uygulama başladıktan sonra:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/api-docs

---

## API Endpoint Özeti

### Kimlik Doğrulama (`/api/auth`)
| Metod | Endpoint | Açıklama |
|-------|----------|----------|
| POST | `/auth/register` | Kayıt (hoş geldin maili gönderilir) |
| POST | `/auth/login` | Giriş |
| POST | `/auth/refresh-token` | Token yenile |
| POST | `/auth/logout` | Çıkış |
| POST | `/auth/forgot-password` | Şifre sıfırlama e-postası gönder |
| POST | `/auth/reset-password` | Şifre sıfırla |
| POST | `/auth/change-password` | Şifre değiştir |

### Ürünler (`/api/products`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/products` | Herkese açık | Arama/filtreleme |
| GET | `/products/featured` | Herkese açık | Öne çıkanlar |
| GET | `/products/{id}` | Herkese açık | Detay |
| GET | `/products/slug/{slug}` | Herkese açık | Slug ile detay |
| POST | `/products` | PRODUCER | Ürün ekle |
| PUT | `/products/{id}` | PRODUCER | Ürün güncelle |
| PATCH | `/products/{id}/toggle-status` | PRODUCER | Aktif/pasif |
| DELETE | `/products/{id}` | PRODUCER | Ürün sil |

### Siparişler (`/api/orders`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| POST | `/orders` | CONSUMER | Sipariş ver |
| GET | `/orders/my` | AUTH | Benim siparişlerim |
| GET | `/orders/{orderNumber}` | AUTH | Sipariş detayı |
| PATCH | `/orders/{id}/cancel` | CONSUMER | İptal et |
| GET | `/orders/producer` | PRODUCER | Üretici siparişleri |
| PATCH | `/orders/{id}/status` | PRODUCER/ADMIN | Durum güncelle |
| GET | `/orders/admin` | ADMIN | Tüm siparişler |

### Ödemeler (`/api/payments`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| POST | `/payments` | AUTH | Ödeme yap |
| GET | `/payments/order/{orderId}` | AUTH | Siparişin ödemesi |
| GET | `/payments/admin` | ADMIN | Tüm ödemeler |
| POST | `/payments/admin/{id}/refund` | ADMIN | İade et |

### Üreticiler (`/api/producers`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/producers/public` | Herkese açık | Üretici ara (bölgeye göre) |
| GET | `/producers/public/{id}` | Herkese açık | Detay |
| POST | `/producers/register` | AUTH | Üretici başvurusu (onay maili) |
| GET | `/producers/me` | PRODUCER | Kendi profil |
| PUT | `/producers/me` | PRODUCER | Profil güncelle |
| POST | `/producers/admin/{id}/approve` | ADMIN | Onayla (mail gönderilir) |
| POST | `/producers/admin/{id}/reject` | ADMIN | Reddet (mail gönderilir) |
| PATCH | `/producers/admin/{id}/commission` | SUPER_ADMIN | Komisyon güncelle |

### Değerlendirmeler (`/api/reviews`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/reviews/product/{productId}` | Herkese açık | Ürün yorumları |
| POST | `/reviews` | CONSUMER | Yorum yaz |
| GET | `/reviews/my` | AUTH | Benim yorumlarım |
| GET | `/reviews/admin/pending` | ADMIN | Onay bekleyenler |
| POST | `/reviews/admin/{id}/approve` | ADMIN | Yorumu onayla |
| DELETE | `/reviews/admin/{id}` | ADMIN | Yorumu sil |

### Favoriler (`/api/favorites`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/favorites` | AUTH | Favori ürünlerim |
| POST | `/favorites/{productId}` | AUTH | Favoriye ekle |
| DELETE | `/favorites/{productId}` | AUTH | Favoriden çıkar |
| GET | `/favorites/{productId}/check` | AUTH | Favoride mi? |

### Adresler (`/api/addresses`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/addresses` | AUTH | Adreslerim |
| POST | `/addresses` | AUTH | Yeni adres |
| PUT | `/addresses/{id}` | AUTH | Güncelle |
| DELETE | `/addresses/{id}` | AUTH | Sil |
| PATCH | `/addresses/{id}/set-default` | AUTH | Varsayılan yap |

### Raporlar (`/api/reports`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/reports/admin/sales` | ADMIN | Satış raporu |
| GET | `/reports/admin/commission` | ADMIN | Komisyon raporu |
| GET | `/reports/admin/top-products` | ADMIN | En çok satanlar |
| GET | `/reports/admin/producers` | ADMIN | Üretici bazlı rapor |
| GET | `/reports/producer/my` | PRODUCER | Kendi raporumu gör |

### Admin (`/api/admin`)
| Metod | Endpoint | Yetki | Açıklama |
|-------|----------|-------|----------|
| GET | `/admin/dashboard` | ADMIN | Dashboard istatistikleri |
| GET | `/admin/users` | ADMIN | Kullanıcılar (filtreli) |
| POST | `/admin/users/{id}/ban` | ADMIN | Kullanıcı banla |
| POST | `/admin/users/{id}/activate` | ADMIN | Aktifleştir |
| GET/PUT | `/admin/settings/{key}` | SUPER_ADMIN | Platform ayarları |

### Lookup (`/api/categories`, `/api/cities`)
| Metod | Endpoint | Açıklama |
|-------|----------|----------|
| GET | `/categories` | Ana kategoriler |
| GET | `/categories/{id}/children` | Alt kategoriler |
| GET | `/cities` | Şehirler |
| GET | `/cities/{id}/districts` | İlçeler |

---

## Proje Yapısı

```
src/main/java/com/pazaryeri/
├── config/          # SecurityConfig, OpenApiConfig
├── controller/      # REST Controller'lar
├── dto/
│   ├── request/    # İstek DTO'ları
│   └── response/   # Yanıt DTO'ları
├── entity/          # JPA Entity sınıfları (15 entity)
├── enums/           # UserRole, OrderStatus, DeliveryType, vb.
├── exception/       # Custom exception'lar + GlobalExceptionHandler
├── repository/      # Spring Data JPA Repository'ler
├── security/        # JwtService, JwtAuthFilter, UserDetailsService
└── service/
    ├── AuthService, ProductService, OrderService...
    └── impl/        # Service implementasyonları
```

---

## Veritabanı Tasarımı

**15 Tablo:**
`users`, `producer_profiles`, `consumer_profiles`, `products`, `product_images`, `categories`, `orders`, `order_items`, `payments`, `reviews`, `favorites`, `commission_transactions`, `platform_settings`, `password_reset_tokens`, `refresh_tokens`, `cities`, `districts`, `addresses`

---

## Varsayılan Admin Hesabı

```
E-posta: admin@pazaryeri.com
Şifre: Admin123!
```

> ⚠️ Production ortamında mutlaka değiştirin!
