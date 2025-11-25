# HeritageHub Marketplace

Spring Boot project that implements the HeritageHub marketplace described by the provided ER diagram. It exposes a REST API for managing admins, sellers, consumers, products, reviews, bids, orders, and transactions while serving a small HTML/CSS/JavaScript dashboard for quick manual testing.

## Tech Stack
- Java 17 + Spring Boot 3 (Web, Data JPA, Validation)
- MySQL 8+ for persistence (Hibernate auto DDL)
- Vanilla HTML, CSS, and JS for the lightweight front-end

## Getting Started
1. By default the app uses an in-memory H2 database so you can run it without any external setup. Simply start the app (see step 3) and browse to the H2 console at `/h2-console` if you want to inspect data.
2. To run against MySQL Workbench instead of H2:
   - Create a schema (e.g., `heritagehub`) in MySQL Workbench.
   - Optionally set the environment variables `MYSQL_USER` / `MYSQL_PASSWORD` (otherwise the defaults `root` / empty string are used).
   - Start the app with the MySQL profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql` (or set `SPRING_PROFILES_ACTIVE=mysql`). The schema will be created/updated automatically (`ddl-auto=update`).
   - Inspect or edit tables directly from MySQL Workbench.
3. API authentication
   - Every account now receives an API key that must be supplied with requests (header `X-API-KEY`).
   - The key is returned from `/auth/register` and `/auth/login`; the front-end stores it in `localStorage`.
   - Admin accounts cannot be self-registered; create them internally or promote an account via the admin dashboard/back-end.
3. Build and run:
   ```bash
   mvn clean package
   ./mvnw spring-boot:run
   ```
   _If Maven needs to download dependencies you will need an internet connection the first time._
4. Visit `http://localhost:8080/` to open the dashboard, or use any REST client against the API under `/api/**`.

## REST API Overview

| Entity | Base Path | Core Operations |
| --- | --- | --- |
| Admin | `/api/admins` | CRUD over platform admins |
| Seller | `/api/sellers` | CRUD, attach to admin via `managerId` |
| Consumer | `/api/consumers` | CRUD for consumer profiles |
| Product | `/api/products` | CRUD, link to seller (and optional admin approver) |
| Review | `/api/reviews` | CRUD, filter by product |
| Bid | `/api/bids` | CRUD + status update, filter by product/consumer |
| Order | `/api/orders` | CRUD + status changes, filter by consumer/status |
| Transaction | `/api/transactions` | Create/list transactions for orders |
| Auth | `/auth/register`, `/auth/login` | Create consumer accounts, authenticate existing users |
| Password Reset | `/auth/forgot-password`, `/auth/reset-password` | Request a verification code and update a user's password |

All POST/PUT requests accept/return JSON. Relationships are wired by query parameters such as `sellerNid`, `productId`, and `consumerNid` to keep payloads lightweight.

## Front-End Dashboard
-`src/main/resources/static/index.html` provides a minimal UI to:
  - Visualise the product catalogue (landing page focused on browsing).
  - Access `seller.html`, `orders.html`, and `reviews.html` for product creation, order logging, review submissions, and integrated product bidding.
- Open `profile.html` to review the information captured during account registration.

The styles (`css/style.css`) and behaviour (`js/app.js`) are plain CSS/JS to keep dependencies minimal.

For account management, use `src/main/resources/static/account.html`, powered by `js/account.js`. It exposes:
- A registration form that POSTs to `/auth/register`.
- A sign-in form that POSTs to `/auth/login` and stores the returned profile in `localStorage` for quick reuse while browsing the console.
- "Forgot password" tooling that hits `/auth/forgot-password` and `/auth/reset-password` (codes are surfaced inline for local demos in lieu of email delivery).
- `profile.html` surfaces the stored account metadata for the currently signed-in user.

## Domain Model Summary
- `Admin` manages sellers and approves products.
- `Seller` publishes `Product` entries.
- `Consumer` can create `Review`, `Bidding`, and `CustomerOrder` records.
- `CustomerOrder` captures quantity, pricing, and delivery details per product/consumer.
- `TransactionRecord` ties a financial record (Bkash/Nogod/Cash/Bank) to an order.

Refer to the entity classes in `src/main/java/com/HeritageHub/model/` for field-level mapping details. All relationships mirror the ER diagram within the constraints of JPA (e.g., `CustomerOrder` is the bridge from products/consumers to transactions).

## Front-End Assets
Place the supplied logo and hero banner inside `src/main/resources/static/images/` using these filenames:

```
src/main/resources/static/images/logo.png   # Weaving logomark
src/main/resources/static/images/hero.jpg   # Hero background featuring the artisan
```

Product cards automatically use `hero.jpg` as a fallback image if an item does not provide `uploadImage` in API responses.

## Next Steps
- Add authentication/authorization (Spring Security) if the platform will be multi-tenant.
- Introduce DTO/request validation rules with `@Valid` constraints for stricter API contracts.
- Implement integration tests using an in-memory MySQL alternative (e.g., Testcontainers) to exercise the API end-to-end.
