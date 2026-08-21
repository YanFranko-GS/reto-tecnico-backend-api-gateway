# Reto Técnico — Microservicios de Pedidos e Inventario

Backend desarrollado con Java 21, Spring Boot 3.5.16, Spring WebFlux, Spring Data JPA, Spring Security, Spring Cloud Gateway, OAuth 2.0 / OpenID Connect con Keycloak, WebClient, OpenAPI/Swagger, JUnit 5, Mockito, AOP y Docker Compose.

## Arquitectura

```text
                          ┌──────────────────┐
                          │     Keycloak     │
                          │      :8090       │
                          └────────┬─────────┘
                                   │ JWT / OIDC
                                   ▼
┌────────────┐              ┌───────────────┐
│   Postman  │─────────────►│ API Gateway   │
└────────────┘              │     :8080     │
                            └───────┬───────┘
                                    │
                         ┌──────────┴──────────┐
                         ▼                     ▼
                  ┌──────────────┐      ┌───────────────┐
                  │ Order Service│─────►│Inventory      │
                  │    :8081     │      │Service :8082  │
                  └──────┬───────┘      └──────┬────────┘
                         │                     │
                         ▼                     ▼
                  ┌─────────────┐       ┌─────────────┐
                  │ PostgreSQL  │       │    MySQL    │
                  │  order_db   │       │ inventory_db│
                  └─────────────┘       └─────────────┘
```

El Gateway es el punto de entrada público. `Order Service` consulta la disponibilidad de `Inventory Service` mediante `WebClient` antes de confirmar un pedido.

## Tecnologías

- Java 21
- Spring Boot 3.5.16
- Spring WebFlux
- Spring Data JPA / Hibernate
- Spring Security
- Spring Cloud Gateway
- OAuth 2.0 / OpenID Connect
- Keycloak 26.7.1
- PostgreSQL 17
- MySQL 8.4
- WebClient
- OpenAPI / Swagger
- JUnit 5
- Mockito
- Reactor Test
- Spring AOP
- Docker / Docker Compose

## Estructura

```text
Reto_Tecnico/
├── api-gateway/
├── order-service/
├── inventory-service/
├── docker-compose.yml
└── README.md
```

## Servicios y puertos

| Servicio | Puerto | Descripción |
|---|---:|---|
| API Gateway | 8080 | Punto de entrada público |
| Order Service | 8081 | Gestión de pedidos |
| Inventory Service | 8082 | Productos y stock |
| Keycloak | 8090 | Autenticación OIDC/JWT |
| PostgreSQL | 5433 | BD de pedidos desde el host |
| MySQL | 3307 | BD de inventario desde el host |

Dentro de Docker los servicios se comunican con nombres de servicio y puertos internos.

## Requisitos

- Java 21
- Docker
- Docker Compose
- Postman (recomendado)

## Ejecución con Docker

Desde la raíz:

```bash
docker compose build
docker compose up -d
docker compose ps
```

Para logs:

```bash
docker compose logs -f api-gateway
docker compose logs -f order-service
docker compose logs -f inventory-service
```

Para detener sin borrar volúmenes:

```bash
docker compose down
```

> Evitar `docker compose down -v` salvo que se quiera borrar deliberadamente la información persistida de PostgreSQL, MySQL y Keycloak.

## Keycloak

Configuración local:

```text
URL: http://localhost:8090
Realm: reto-backend
Cliente OIDC: reto-api
Standard Flow: ON
Direct Access Grants: OFF
PKCE: S256
```

Issuer:

```text
http://localhost:8090/realms/reto-backend
```

El usuario de pruebas se configura en Keycloak y obtiene tokens mediante Authorization Code + PKCE.

Well-known:

```text
http://localhost:8090/realms/reto-backend/.well-known/openid-configuration
```

## JWT

Los endpoints protegidos requieren:

```http
Authorization: Bearer <JWT>
```

Sin token:

```text
401 Unauthorized
```

Con JWT válido emitido por Keycloak:

```text
acceso permitido
```

Gateway, Order Service e Inventory Service validan el JWT.

## X-Trace-Id

El Gateway recibe o genera `X-Trace-Id` y lo propaga:

```text
Cliente
   ↓
Gateway
   ↓
Order Service
   ↓
Inventory Service
```

El mismo identificador se usa en:

- headers
- respuestas
- errores
- logs
- llamadas internas de WebClient

Ejemplo:

```http
X-Trace-Id: docker-final-001
```

Esto permite seguir una petición completa a través de los microservicios.

## Endpoints

### Orders

Base por Gateway:

```text
http://localhost:8080/api/v1/orders
```

Crear:

```http
POST /api/v1/orders
```

Body:

```json
{
  "productoId": 1,
  "cantidad": 2
}
```

Consultar:

```http
GET /api/v1/orders/{orderId}
```

Historial:

```http
GET /api/v1/orders/{orderId}/history
```

Cancelar:

```http
POST /api/v1/orders/{orderId}/cancel
```

### Inventory

Base por Gateway:

```text
http://localhost:8080/api/v1/inventario/producto
```

Producto:

```http
GET /api/v1/inventario/producto/{productoId}
```

Disponibilidad:

```http
GET /api/v1/inventario/producto/{productoId}/disponibilidad?cantidadSolicitada=2
```

## Flujo de creación de pedido

```text
POST /api/v1/orders
        ↓
API Gateway
        ↓
Validación JWT
        ↓
Order Service
        ↓
WebClient + JWT + X-Trace-Id
        ↓
Inventory Service
        ↓
Validación de stock
        ↓
Stock disponible
        ↓
CONFIRMED
        ↓
Persistencia de Order + historial
```

Sin stock suficiente:

```text
409 Conflict
STOCK_INSUFFICIENT
```

## Estados

```text
PENDING   → CANCELLED    permitido
CONFIRMED → CANCELLED    permitido
CANCELLED → CANCELLED    rechazado
```

Los cambios se registran en `order_status_history`.

## Formato de error

Ejemplo:

```json
{
  "timestamp": "2026-08-21T10:00:00Z",
  "status": 409,
  "code": "STOCK_INSUFFICIENT",
  "message": "Stock insuficiente para el producto con id: 1",
  "traceId": "docker-final-001"
}
```

Casos contemplados:

```text
400 INVALID_REQUEST
401 Unauthorized
404 ORDER_NOT_FOUND
404 PRODUCTO_NO_ENCONTRADO
409 STOCK_INSUFFICIENT
409 INVALID_ORDER_TRANSITION
500 INTERNAL_ERROR
```

## Swagger / OpenAPI

Order Service:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

Inventory Service:

```text
http://localhost:8082/swagger-ui.html
http://localhost:8082/v3/api-docs
```

Las APIs de negocio permanecen protegidas con JWT.

## Pruebas

Order Service:

```bash
cd order-service
./mvnw test
```

Inventory Service:

```bash
cd inventory-service
./mvnw test
```

Las pruebas cubren creación con stock, rechazo por stock insuficiente, consultas, producto inexistente y transiciones inválidas.

## AOP

Se utiliza Spring AOP para logging transversal de servicios:

```text
inicio
fin
duración
error
```

Ejemplo:

```text
Iniciando InventoryService.verificarDisponibilidad(..)
[traceId=docker-final-001] Producto 1 - stock=10, cantidadSolicitada=2, disponible=true
Finalizando InventoryService.verificarDisponibilidad(..) - duración=40 ms
```

## Datos de prueba

Los datos iniciales están separados por microservicio:

```text
order-service/src/main/resources/import.sql
inventory-service/src/main/resources/import.sql
```

Inventory incluye, entre otros:

```text
producto 1 → Laptop Lenovo IdeaPad → stock 10
producto 2 → Mouse Logitech M185   → stock 25
producto 3 → Teclado Redragon      → stock 8
producto 4 → Monitor LG            → stock 0
producto 5 → SSD Kingston          → stock 3
```

## Smoke test final

1. Levantar Docker Compose.
2. Obtener un JWT nuevo desde Keycloak.
3. Ejecutar:

```http
POST http://localhost:8080/api/v1/orders
```

Headers:

```http
Authorization: Bearer <JWT>
X-Trace-Id: docker-final-001
Content-Type: application/json
```

Body:

```json
{
  "productoId": 1,
  "cantidad": 2
}
```

Esperado:

```text
201 Created
estado = CONFIRMED
```

Luego probar una cantidad superior al stock para obtener:

```text
409 STOCK_INSUFFICIENT
```

Y una petición sin JWT para comprobar:

```text
401 Unauthorized
```

## Nota

Las credenciales y contraseñas incluidas en Docker Compose son exclusivamente para desarrollo local y demostración del reto. No deben reutilizarse en producción.