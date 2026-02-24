# Newsstand Management System

A full-stack web application for managing newsstand inventory and sales, built with a separated frontend and backend architecture.

## Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Sprint 1 Deliverables](#sprint-1-deliverables)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Architecture Principles](#architecture-principles)

---

## Quick Access

| Resource | URL |
|----------|-----|
| **Frontend Demo** | http://34.194.175.52/products |
| **Backend API Docs (Swagger)** | http://34.194.175.52:8080/swagger-ui/index.html |

---

## Project Overview

### Product Vision

The Newsstand Management System is a digital transformation solution designed to help small newsstands replace manual bookkeeping with efficient inventory management and sales processing.

### Business Goals

| Goal | Description |
|------|-------------|
| **Primary** | Replace manual bookkeeping and improve operational efficiency |
| **Secondary** | Reduce inventory errors and provide foundation for sales analytics |
| **Long-term** | Support multi-newsstand chain management |

### Team & Timeline

| Aspect | Details |
|--------|---------|
| **Team Size** | 2 developers (1 Backend + 1 Frontend) |
| **Project Duration** | 2 Sprints (6 weeks) for MVP |
| **Development Methodology** | Agile/Scrum with 3-week sprints |

---

## Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.5.6 | Application framework |
| **MyBatis-Plus** | 3.5.11 | ORM framework |
| **MySQL** | 8.4.0 | Database |
| **SpringDoc OpenAPI** | 2.8.10 | API documentation |
| **Lombok** | Latest | Reduce boilerplate |
| **UUID Creator** | 5.3.7 | UUIDv7 generation |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.1.1 | UI framework |
| **Ant Design** | 6.2.3 | UI component library |
| **TanStack Query** | 5.90.20 | Data fetching & caching |
| **Emotion CSS** | 11.13.5 | CSS-in-JS styling |
| **Axios** | 1.13.4 | HTTP client |
| **Vite** | 7.1.14 | Build tool |

---

## Sprint 1 Deliverables

### Epic 1: Product and Inventory Management

**Priority:** P0 (Critical) | **Status:** ✅ Completed

Sprint 1 focused on building the foundational product and inventory management capabilities required for all sales operations.

### Completed User Stories

#### US 1.1: Add New Product (3 SP)

**Story:** As a store clerk, I want to add new newspapers or magazines to the system so that I can sell new products.

**Implementation:**
- **Backend:** `POST /api/products` - Creates products with validation for name uniqueness, price > 0, stock >= 0
- **Frontend:** `CreateProductModal.tsx` - Form with dropdown for product type (Newspaper/Magazine)

**Acceptance Criteria:** ✅ All met
- Product name cannot be empty or duplicated
- Price must be greater than 0
- Initial stock must be non-negative
- Success confirmation displayed after creation

---

#### US 1.2: View Product List (5 SP)

**Story:** As a store clerk, I want to view all products in a list so that I know what's currently available.

**Implementation:**
- **Backend:** `GET /api/products` - Paginated query with type filtering (NEWSPAPER/MAGAZINE)
- **Frontend:** `ProductTable.tsx` - Table display with red highlighting for zero-stock items

**Acceptance Criteria:** ✅ All met
- Displays product name, type, price, and stock
- Sorted by creation time (descending)
- Type filtering support
- Pagination support (default 20 items per page)
- Zero-stock items marked in red

---

#### US 1.3: Update Product Information (3 SP)

**Story:** As a store clerk, I want to modify existing product information so that I can adjust prices or other details.

**Implementation:**
- **Backend:** `PUT /api/products/{id}` - Updates name, type, and price (stock excluded)
- **Frontend:** `EditProductModal.tsx` - Pre-filled edit form

**Acceptance Criteria:** ✅ All met
- Edit button on each product row
- Modal pre-fills current product data
- Can modify: name, type, price
- Stock cannot be modified through this endpoint

---

#### US 1.4: Adjust Stock Quantity (5 SP)

**Story:** As a store clerk, I want to manually adjust product stock so that I can correct inventory errors or replenish stock.

**Implementation:**
- **Backend:** `POST /api/products/{id}/adjust-stock` - Accepts positive/negative quantity with validation
- **Frontend:** `AdjustStockModal.tsx` + `AdjustStockForm.tsx` - Real-time stock preview

**Acceptance Criteria:** ✅ All met
- Supports positive (increase) and negative (decrease) quantities
- Displays projected stock after adjustment
- Prevents negative stock values
- Optional reason input for audit trail

---

#### US 1.5: Delete Product (2 SP)

**Story:** As a store clerk, I want to stop selling a product so that I can manage discontinued items.

**Implementation:**
- **Backend:** `DELETE /api/products/{id}` - Soft delete (sets `deleted=true`)
- **Frontend:** Confirmation dialog before deletion

**Acceptance Criteria:** ✅ All met
- Delete button on each product row
- Confirmation dialog shows product name
- Soft delete implementation (data preserved)
- Deleted products excluded from list queries

---

#### US 1.6: Low Stock Alert (3 SP)

**Story:** As a store clerk, I want to see products with insufficient stock so that I can replenish in time.

**Implementation:**
- **Backend:** `GET /api/products/low-stock` - Returns products with stock <= threshold (default 10)
- **Frontend:** `LowStockAlert.tsx` + `LowStockTab.tsx` - Alert banner and dedicated tab

**Acceptance Criteria:** ✅ All met
- Alert area at top of product list page
- Products with stock <= 10 highlighted
- Yellow/orange background for visibility
- Dedicated "Low Stock" tab for detailed view

---

### Sprint 1 Summary

| Metric | Value |
|--------|-------|
| **Total Story Points** | 21 |
| **User Stories Completed** | 6/6 |
| **Backend APIs Delivered** | 7 endpoints |
| **Frontend Components** | 8 components |
| **Status** | ✅ Complete |

### Code Quality Report (SonarCloud)

![SonarCloud Snapshot](Backend/docs/sonarcloud-snapshot/sonarcloud-snapshot.png)

---

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Product Management Endpoints

#### Create Product

```http
POST /products
Content-Type: application/json

{
  "name": "Daily News",
  "type": "NEWSPAPER",
  "price": 2.50,
  "stock": 100
}
```

#### Query Products (Paginated)

```http
GET /products?page=0&size=20&type=NEWSPAPER
```

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| page | int | Page number (starts from 0) | 0 |
| size | int | Items per page (1-100) | 20 |
| type | string | Filter by type (NEWSPAPER/MAGAZINE) | - |

#### Get Product by ID

```http
GET /products/{id}
```

#### Update Product

```http
PUT /products/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  "type": "MAGAZINE",
  "price": 5.99
}
```

#### Adjust Stock

```http
POST /products/{id}/adjust-stock
Content-Type: application/json

{
  "quantity": -5,
  "reason": "Damaged goods"
}
```

#### Delete Product

```http
DELETE /products/{id}
```

#### Get Low Stock Products

```http
GET /products/low-stock?page=0&size=20&threshold=10
```

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| page | int | Page number (starts from 0) | 0 |
| size | int | Items per page (1-100) | 20 |
| threshold | int | Stock threshold | 10 |

### Response Format

All API responses follow this structure:

**Success response:**
```json
{
  "success": true,
  "errorMsg": null,
  "data": { ... },
  "total": 100,
  "totalPages": 5
}
```

**Error response:**
```json
{
  "success": false,
  "errorMsg": "Error description",
  "data": null,
  "total": null,
  "totalPages": null
}
```

### Error Responses

| HTTP Status | Error Type | Description |
|-------------|------------|-------------|
| 400 | BusinessException / ValidationException | Business logic error or request validation failed |
| 404 | NotFoundException | Resource not found |
| 409 | ConflictException | Resource conflict (e.g., duplicate name) |
| 500 | Exception | Unexpected server error |

---

## Project Structure

```
newsagent-management-system/
├── Backend/
│   ├── docs/                    # Documentation
│   │   ├── PRD.md
│   │   ├── Task-Assignment.md
│   │   └── ...
│   └── Newsstand_Backend_System/
│       ├── src/main/java/org/shining319/newsstand_backend_system/
│       │   ├── config/          # Configuration classes
│       │   │   ├── CorsConfig.java
│       │   │   ├── MybatisPlusConfig.java
│       │   │   ├── SwaggerConfig.java
│       │   │   └── ...
│       │   ├── controller/      # REST controllers
│       │   │   └── ProductController.java
│       │   ├── service/         # Business logic
│       │   │   ├── IProductService.java
│       │   │   └── impl/
│       │   │       └── ProductServiceImpl.java
│       │   ├── dao/            # Data access layer
│       │   │   └── ProductMapper.java
│       │   ├── entity/         # JPA/MyBatis entities
│       │   │   ├── Product.java
│       │   │   └── ProductTypeEnum.java
│       │   ├── dto/            # Data transfer objects
│       │   │   ├── request/
│       │   │   │   ├── CreateProductRequest.java
│       │   │   │   ├── UpdateProductRequest.java
│       │   │   │   ├── AdjustStockRequest.java
│       │   │   │   └── ...
│       │   │   └── response/
│       │   │       ├── ProductVO.java
│       │   │       └── Result.java
│       │   ├── exception/      # Custom exceptions
│       │   │   ├── BusinessException.java
│       │   │   ├── NotFoundException.java
│       │   │   ├── ConflictException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   └── util/           # Utility classes
│       │       └── UuidUtil.java
│       ├── src/main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   ├── application-test.yml
│       │   └── application-prod.yml
│       └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/                # API integration
│   │   │   ├── endpoints/
│   │   │   │   ├── product-management.ts
│   │   │   │   └── newsstandManagementSystemAPI.schemas.ts
│   │   │   └── request.ts
│   │   ├── constants/          # Constants
│   │   │   └── product.ts
│   │   ├── layouts/            # Layout components
│   │   │   └── MainLayout.tsx
│   │   ├── pages/              # Page components
│   │   │   └── products/
│   │   │       ├── index.tsx
│   │   │       ├── components/
│   │   │       │   ├── CreateProductModal.tsx
│   │   │       │   ├── EditProductModal.tsx
│   │   │       │   ├── AdjustStockModal.tsx
│   │   │       │   ├── AdjustStockForm.tsx
│   │   │       │   ├── LowStockAlert.tsx
│   │   │       │   ├── LowStockTab.tsx
│   │   │       │   ├── ProductDetailModal.tsx
│   │   │       │   └── ProductTable.tsx
│   │   │       └── hooks/
│   │   │           └── useAdjustStockForm.ts
│   │   ├── utils/
│   │   │   └── format.ts
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

---

## Architecture Principles

### Overall Design

The system follows a strict **frontend-backend separation** architecture:
- **Frontend**: React SPA communicates with the backend exclusively via REST API
- **Backend**: Spring Boot provides stateless RESTful APIs
- **Database**: MySQL handles persistence; all business logic lives in the application layer

---

### Backend

#### Layered Architecture

The backend enforces strict separation of responsibilities across three layers:

| Layer | Responsibility |
|-------|----------------|
| **Controller** | HTTP handling, parameter validation (`@Valid`), wrapping responses into `Result<T>` |
| **Service** | Business logic only — returns business objects, never `Result`; signals failures by throwing exceptions |
| **DAO/Mapper** | Data access via MyBatis-Plus + custom XML SQL |

**Key rule:** The Service layer never returns `Result` objects. Using exceptions (rather than `Result.fail()`) to signal failure ensures that `@Transactional` rollback works correctly.

#### Exception Handling

Custom exceptions are organized in an inheritance hierarchy, all mapped to HTTP status codes by `GlobalExceptionHandler`:

```
RuntimeException
  └── BusinessException      → 400 Bad Request
        ├── NotFoundException  → 404 Not Found
        └── ConflictException  → 409 Conflict
MethodArgumentNotValidException → 400 Bad Request (validation failures)
Exception (catch-all)          → 500 Internal Server Error
```

Controllers never catch exceptions directly — `GlobalExceptionHandler` handles all mapping uniformly.

#### Parameter Validation

Validation rules are declared in Request DTOs using Bean Validation (JSR-380) annotations (`@NotBlank`, `@Min`, `@DecimalMin`, etc.). The Controller triggers validation with `@Valid`. On failure, `GlobalExceptionHandler` formats all field errors into a single `"field: message; field: message"` string.

#### Concurrency Control

- **Optimistic locking**: The `Product` entity has a `@Version` field (`version`). The stock adjustment SQL manually checks `AND version = #{version}` and increments it. If 0 rows are affected, a `BusinessException` is thrown.

---

### Frontend

#### API Integration

The API layer is **code-generated from the OpenAPI specification** using Orval:
- `endpoints/newsstandManagementSystemAPI.schemas.ts` — Generated TypeScript types only (no runtime code)
- `endpoints/product-management.ts` — Generated TanStack Query hooks and raw fetcher functions
- `api/request.ts` — Hand-written Axios instance; the response interceptor unwraps the backend's `{ success, errorMsg, data }` envelope into normal Promise resolution/rejection

This means frontend types are always in sync with the backend contract — a backend change only requires re-running the code generator.

#### State Management

- **Server state**: Managed exclusively by TanStack Query — no Redux or global store
- **Local UI state**: Plain `useState` (modal visibility, selected items, pagination)
- **Cache invalidation**: After every mutation, invalidate all relevant query keys (product list + low-stock list) to keep views consistent

#### Component Design

| Component Role | Pattern |
|---|---|
| **Page (`index.tsx`)** | Orchestrator — owns all modal state, pagination, and mutation handlers; passes callbacks as props |
| **Table / Form** | Purely presentational — receives data and callbacks via props; makes no API calls |
| **Modal** | Semi-autonomous — owns its own form, mutation, and cache invalidation |
| **Custom hook** | Extracted when the same form + mutation logic is reused across multiple contexts |

#### Routing & Styling

- Routes are **auto-generated** from the `src/pages/` file tree via `vite-plugin-pages` — no manual route registration needed
- Styles are written using `@emotion/css` co-located with JSX; all colors and spacing use Ant Design design tokens via `theme.useToken()`

---

### Database

#### Table Design Principles

- **No physical foreign keys** — cross-table relationships are expressed in SQL comments only, reducing coupling and improving write throughput
- **Data snapshots** — `sale_items` stores `product_name` and `unit_price` as denormalized snapshot fields to preserve historical order integrity even after products are modified or deleted
- **Monetary values** — all price/amount fields use `DECIMAL(10, 2)` to avoid floating-point precision issues
- **Index discipline** — no more than 5 indexes per table, covering high-frequency query paths (type filter, stock threshold, creation time sort, name uniqueness)

#### Soft Delete

All deletes are logical (soft) using a `deleted` boolean field managed by MyBatis-Plus `@TableLogic`:

- `deleted = false` → active record
- `deleted = true` → logically deleted, excluded from all queries
- **Delete is idempotent** — deleting an already-deleted record returns success silently
- **Name reuse after deletion** — the unique index is on `(name, deleted)` as a composite, so a deleted product's name can be reused for a new product

#### ID Generation Strategy

Primary keys use **UUIDv7** (time-ordered UUID):
- Generated at the **application layer** (not the database) using the `uuid-creator` library
- Stored as `BINARY(16)` in MySQL for compact storage and efficient B-Tree indexing
- A custom `UuidBinaryTypeHandler` handles bidirectional conversion between Java `String` and MySQL `BINARY(16)`
- UUIDv7's time-ordered prefix avoids the index page splits caused by UUIDv4's randomness

#### Pagination

- `PaginationInnerInterceptor` (MyBatis-Plus) automatically injects `LIMIT` and executes `COUNT(*)` — no `LIMIT` clause is written in XML SQL
- **Page index convention**: frontend sends 0-based page numbers; the Service layer adds `+1` before passing to MyBatis-Plus (which is 1-based)
