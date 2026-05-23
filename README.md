# Telco CRM Platform

Spring Boot 4.0.6 ve Spring Cloud 2025.1.1 tabanlı, çok modüllü Maven mikroservis projesi.

## Ekip

- Adil Arkalı
- Erol Koçoğlu
- Ayşe Ulaşlı
- Eren Eray Yılmaz

## Teknoloji Stack'i

- **Java 21**
- **Spring Boot 4.0.6** — `webmvc` starter
- **Spring Cloud 2025.1.1** — Netflix Eureka, Spring Cloud Gateway (MVC)
- **PostgreSQL 16** — servis başına ayrı veritabanı (database-per-service)
- **Flyway** — veritabanı migration yönetimi
- **Docker Compose** — yerel altyapı (PostgreSQL'ler + pgAdmin)
- **Maven** — multi-module build

## Servisler

| Servis | Port | Veritabanı | DB Host Port |
|---|---|---|---|
| eureka-server | 8761 | — | — |
| gateway-server | 8888 | — | — |
| identity-service | 8081 | identity_db | 5433 |
| customer-service | 8082 | customer_db | 5434 |
| product-catalog-service | 8083 | catalog_db | 5435 |
| order-service | 8084 | order_db | 5436 |
| subscription-service | 8085 | subscription_db | 5437 |
| usage-service | 8086 | usage_db | 5438 |
| billing-service | 8087 | billing_db | 5439 |
| payment-service | 8088 | payment_db | 5440 |
| notification-service | 8089 | notification_db | 5441 |
| ticket-service | 8090 | ticket_db | 5442 |
| pgAdmin | 5151 | — | — |

## Gateway Rotaları

Tüm istemci trafiği `http://localhost:8888` üzerinden akar:

| Path | Hedef servis |
|---|---|
| `/api/auth/**`, `/api/identity/**` | identity-service |
| `/api/customers/**` | customer-service |
| `/api/catalog/**`, `/api/products/**` | product-catalog-service |
| `/api/orders/**` | order-service |
| `/api/subscriptions/**` | subscription-service |
| `/api/usage/**` | usage-service |
| `/api/billing/**`, `/api/invoices/**` | billing-service |
| `/api/payments/**` | payment-service |
| `/api/notifications/**` | notification-service |
| `/api/tickets/**` | ticket-service |

## Başlangıç

### 1. Altyapıyı ayağa kaldır

```bash
docker-compose up -d
```

PostgreSQL container'ları ve pgAdmin başlar.

### 2. Tüm modülleri derle

```bash
./mvnw clean install -DskipTests
```

### 3. Servisleri çalıştır

Bağımlılık sırası: önce **eureka**, ardından **gateway** ve diğer servisler.

```bash
# Terminal 1 - Eureka (8761)
./mvnw -pl eureka-server spring-boot:run

# Terminal 2 - Gateway (8888)
./mvnw -pl gateway-server spring-boot:run

# Terminal 3 - Customer service (8082)
./mvnw -pl customer-service spring-boot:run

# diğer servisler için aynı kalıp:
./mvnw -pl <servis-adı> spring-boot:run
```

### 4. Doğrulama

- Eureka dashboard: <http://localhost:8761>
- Gateway rotaları: <http://localhost:8888/actuator/gateway/routes>
- pgAdmin: <http://localhost:5151>
- Servis health (gateway üzerinden): <http://localhost:8888/api/customers/actuator/health>

## Mimari

- **Database-per-Service** — her mikroservis kendi PostgreSQL şemasına sahip
- **API Gateway** — Spring Cloud Gateway MVC ile tek giriş noktası
- **Service Discovery** — Netflix Eureka ile servis kayıt/keşif
- **Flyway** — versiyonlu DB migration'ları (`src/main/resources/db/migration`)

### Hedeflenen Genişlemeler

- Transactional Outbox pattern (event-driven servisler)
- Saga pattern (order akışı için)
- JWT tabanlı kimlik doğrulama (identity-service)

## Proje Yapısı

```
telco-crm-platform/
├── pom.xml                       # parent pom, ortak bağımlılıklar
├── docker-compose.yml            # PostgreSQL'ler + pgAdmin
├── common-lib/                   # paylaşılan DTO/exception kütüphanesi
├── eureka-server/                # service registry
├── gateway-server/               # API gateway
├── identity-service/             # kimlik doğrulama, JWT
├── customer-service/             # müşteri yönetimi
├── product-catalog-service/      # ürün/tarife katalogu
├── order-service/                # sipariş akışı
├── subscription-service/         # abonelik yönetimi
├── usage-service/                # kullanım verileri
├── billing-service/              # faturalama
├── payment-service/              # ödeme
├── notification-service/         # bildirim
└── ticket-service/               # destek/talep
```
