# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Business context

Marquesitas started as a digitization project for a single snack stand (in a park) that currently manages orders on paper. The business model: the customer orders and pays upfront, staff prepares and tracks the order, and the day ends with a cash-register close-out ("corte de caja"). The intended product is an offline-first PWA (optimized for iPad/tablet) that lets staff take orders with zero connectivity dependency, syncing to a central backend once connectivity returns.

This repository (`marquesitasapi`) is that central backend API — the source of truth once data syncs from the field device. The offline client itself lives elsewhere and is out of scope here.

The longer-term vision is a multi-tenant platform serving many snack stands from one central data store, enabling cross-business analytics and eventually demand-forecasting / ML-driven recommendations. The current schema and code are single-tenant (no business/tenant identifier anywhere yet) — keep that in mind if asked to add multi-tenancy: it's a known future direction, not yet designed.

## Commands

Use the Maven wrapper (`mvnw.cmd` on Windows / `./mvnw` on Unix-likes) — do not assume a global Maven install.

```
mvnw.cmd clean install          # full build + tests
mvnw.cmd test                   # run all tests
mvnw.cmd test -Dtest=OrderServiceImplTest                       # run a single test class
mvnw.cmd test -Dtest=OrderServiceImplTest#create_shouldSaveOrder # run a single test method
mvnw.cmd spring-boot:run         # run the app locally (port 8081 by default)
```

The app requires a running PostgreSQL instance matching `marquesitas-bd.sql`. Connection is configured via env vars with local defaults (see `src/main/resources/application.yaml`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`. `spring.jpa.hibernate.ddl-auto` is `validate` — schema changes must be made in `marquesitas-bd.sql` and applied to the database directly; Hibernate will not auto-migrate.

Postgres normally runs via `docker-compose.yml` (`docker compose up -d`), mapped to host port 5433 to avoid clashing with a locally-installed Postgres. To use a locally-installed Postgres (standard port 5432, same db/user/password) instead of Docker, activate the `dev-postgres` profile — `src/main/resources/application-dev-postgres.yaml` overrides `spring.datasource.url` accordingly: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev-postgres`, or set `SPRING_PROFILES_ACTIVE=dev-postgres`. That local instance also needs `marquesitas-bd.sql` applied manually beforehand — same caveat as the Docker instance.

Swagger UI is served at `/swagger-ui.html`, OpenAPI JSON at `/v3/api-docs`.

## Architecture

Standard layered structure, one package per concern under `com.josemaba.marquesitasapi`:

- `controller` — REST endpoints, one per aggregate (Category, Product, ProductAddon, ProductDetail, Order). Delegates directly to services; no business logic here.
- `service` / `service.impl` — interface + implementation per aggregate. All business rules and transactions (`@Transactional`) live here.
- `repository` — Spring Data JPA repositories.
- `entity` — JPA entities, all using `UUID` (GenerationType.UUID) primary keys, Lombok `@Builder`/`@Getter`/`@Setter`, and `@EqualsAndHashCode(of = "id")`.
- `dto.request` / `dto.response` — request/response DTOs (records), decoupled from entities.
- `mapper` — MapStruct mappers (`componentModel = "spring"`) converting between entities and DTOs; injected as Spring beans.
- `exception` / `GlobalExceptionHandler` — centralized `@RestControllerAdvice` translates domain exceptions to HTTP responses: `ResourceNotFoundException` → 404, `DuplicateResourceException`/`BusinessRuleViolationException` → 409, validation errors → 400 with field-level details, `DataIntegrityViolationException` → 409, anything else → 500 (logged).

### Domain model

- `Category` 1—N `Product`.
- `Product` 1—N `ProductDetail`, where `ProductDetail` is the join entity linking a `Product` to the `ProductAddon`s it's allowed to be ordered with (unique on `product_id, addon_id`). This is a catalog-level association, not part of an order.
- `Order` 1—N `OrderDetail` (line items) 1—N `OrderDetailAddon` (addons selected for that line item, snapshotting `addonName`/`unitPrice` at order time so historical orders are unaffected by later catalog price changes).
- `Order.orderDetails` and `OrderDetail.orderDetailAddons` cascade `ALL` + `orphanRemoval` from their parent — build/modify order graphs by setting the collection on the parent (see `OrderServiceImpl.buildOrderDetail`), not by saving child entities directly.
- `Order.orderNumber` is a DB-generated, read-only sequential column (`@Generated(event = EventType.INSERT)`, `insertable/updatable = false`) — never set it from application code.

### Order business rules (`OrderServiceImpl`)

- Order status transitions are constrained by the `ALLOWED_TRANSITIONS` map: `PENDING → {IN_PROGRESS, CANCELLED}`, `IN_PROGRESS → {READY, CANCELLED}`, `READY → {COMPLETED, CANCELLED}`; `COMPLETED`/`CANCELLED` are terminal. Any other transition throws `BusinessRuleViolationException`.
- When creating an order, each addon selected on a line item must (a) exist, (b) be `available`, and (c) be linked to that product via `ProductDetail` — otherwise a `BusinessRuleViolationException` is thrown.
- Line subtotals are computed server-side as `(product.price + sum(addon prices)) * quantity`, scaled to 2 decimals with `HALF_UP` rounding — never trust client-supplied totals.
- Payment happens up front (customer pays before preparation begins), matching the real-world "corte de caja" flow — `Order` already carries `paymentMethod` and `completedAt`, but there is no cash-register close-out / reporting endpoint yet.

### Testing conventions

Service-layer unit tests (`src/test/.../service/impl/*Test.java`) use `MockitoExtension` with `@Mock` repositories/mappers and `@InjectMocks` on the service impl — no Spring context is loaded. Follow the existing `given(...)`/`assertThat(...)` (AssertJ + BDDMockito) style and naming pattern `methodName_shouldX_whenY`.
