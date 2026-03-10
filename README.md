# Digital Marketplace Backend

Backend API for a **local producer marketplace platform** where farmers, artisans and homemade product sellers can sell directly to consumers.

The project is built with **Spring Boot 3**, implements **JWT based authentication**, **role-based authorization**, and supports a complete **e-commerce workflow including products, orders, payments, reviews and admin management**.

---

# Project Overview

This platform enables a **multi-role marketplace system** with separate capabilities for administrators, producers, and consumers.

Typical flow:

Consumer → Browse products → Place order → Payment → Producer fulfills order → Review system

---

# Tech Stack

| Layer              | Technology                  |
| ------------------ | --------------------------- |
| Backend Framework  | Spring Boot 3               |
| Language           | Java 17                     |
| Security           | Spring Security + JWT       |
| Database           | PostgreSQL                  |
| ORM                | Spring Data JPA (Hibernate) |
| Database Migration | Flyway                      |
| API Documentation  | Swagger / OpenAPI           |
| Build Tool         | Maven                       |
| Containerization   | Docker                      |

---

# Key Features

### Authentication & Security

* JWT authentication
* Refresh token mechanism
* Password reset via email
* Role based authorization

### Marketplace System

* Product management
* Category system
* Product images
* Product search and filtering

### Order Management

* Create orders
* Order status workflow
* Producer order dashboard
* Order cancellation

### Payment System

* Payment processing
* Refund capability
* Payment tracking

### Review System

* Product reviews
* Review moderation by admin

### Favorites

* Users can favorite products
* Favorite status check

### Producer Management

* Producer application system
* Admin approval/rejection
* Commission configuration

### Reporting System

* Sales reports
* Commission reports
* Top selling products
* Producer performance reports

### Admin Panel

* Dashboard statistics
* User management
* Producer management
* Platform settings

---

# User Roles

| Role        | Description                             |
| ----------- | --------------------------------------- |
| SUPER_ADMIN | Full system control and configuration   |
| ADMIN       | Platform moderation and user management |
| PRODUCER    | Store management and product selling    |
| CONSUMER    | Product browsing and purchasing         |

---

# API Documentation

After running the application:

Swagger UI
http://localhost:8080/api/swagger-ui.html

OpenAPI JSON
http://localhost:8080/api/api-docs

Swagger provides full interactive documentation for all endpoints.

---

# Installation

## Requirements

* Java 17+
* Maven 3.8+
* PostgreSQL 15+

---

## 1 Create Database

```sql
CREATE DATABASE pazaryeri_db;

CREATE USER pazaryeri_user WITH PASSWORD 'your_password';

GRANT ALL PRIVILEGES ON DATABASE pazaryeri_db TO pazaryeri_user;
```

---

## 2 Environment Variables

```bash
export DB_USERNAME=pazaryeri_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_256_bit_secret_key
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password
```

---

## 3 Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

---

# Run With Docker

```bash
docker-compose up -d
```

---

# Project Structure

```
src/main/java/com/pazaryeri

config/         Security & OpenAPI configuration  
controller/     REST Controllers  
dto/            Request and response models  
entity/         JPA entities  
enums/          Application enums  
exception/      Global exception handling  
repository/     Spring Data repositories  
security/       JWT authentication logic  
service/        Business logic layer  
service/impl/   Service implementations
```

---

# Database Overview

Main tables used in the system:

users
producer_profiles
consumer_profiles
products
product_images
categories
orders
order_items
payments
reviews
favorites
commission_transactions
platform_settings
password_reset_tokens
refresh_tokens
cities
districts
addresses

---

# Default Admin Account

```
Email: admin@pazaryeri.com
Password: Admin123!
```

Change these credentials before using in production.

---

---

# Türkçe

## Dijital Pazar Yeri Backend

Bu proje, **yerel üreticilerin (çiftçiler, zanaatkarlar, ev yapımı ürün satıcıları)** ürünlerini doğrudan tüketicilere satabilecekleri bir **dijital pazar yeri platformunun backend API'sidir.**

Sistem aşağıdaki ana özellikleri içerir:

* JWT tabanlı kimlik doğrulama
* Rol bazlı yetkilendirme
* Ürün ve kategori yönetimi
* Sipariş sistemi
* Ödeme sistemi
* Yorum ve değerlendirme sistemi
* Favori ürünler
* Admin paneli
* Raporlama sistemi

---

## Kullanıcı Rolleri

| Rol         | Açıklama                                 |
| ----------- | ---------------------------------------- |
| SUPER_ADMIN | Platform ayarları ve tam sistem kontrolü |
| ADMIN       | Kullanıcı ve üretici yönetimi            |
| PRODUCER    | Ürün ekleme ve sipariş yönetimi          |
| CONSUMER    | Ürün görüntüleme ve sipariş verme        |

---

## Kurulum

### Gereksinimler

* Java 17+
* Maven 3.8+
* PostgreSQL

---

### Veritabanı oluşturma

```sql
CREATE DATABASE pazaryeri_db;
```

---

### Uygulamayı çalıştırma

```bash
mvn clean install
mvn spring-boot:run
```

---

### Docker ile çalıştırma

```bash
docker-compose up -d
```

---

## API Dokümantasyonu

Uygulama çalıştıktan sonra Swagger arayüzüne şu adresten ulaşabilirsiniz:

http://localhost:8080/api/swagger-ui.html
