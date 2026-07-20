# PuraVida Backend

API REST del MVP PuraVida, preparada como monolito modular con arquitectura hexagonal ligera.

## Requisitos

- JDK 21.
- Maven 3.8+.
- MySQL para etapas futuras de integracion.

El proyecto compila usando `release` 21.

## Ejecucion

```bash
mvn spring-boot:run
```

La API usa por defecto el puerto `8080`.

## Build y pruebas

```bash
mvn test
mvn package -DskipTests
```

Si mas adelante se agrega Maven Wrapper, los comandos equivalentes seran `./mvnw test` y `./mvnw package -DskipTests`.

## Configuracion

La configuracion base esta en `src/main/resources/application.yml`.

Variables esperadas para una futura conexion a MySQL:

- `PURAVIDA_DB_URL`
- `PURAVIDA_DB_USERNAME`
- `PURAVIDA_DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `PURAVIDA_LOGO_STORAGE_ROOT`
- `PURAVIDA_LOGO_MAX_BYTES`

No se incluyen secretos ni credenciales reales. El usuario por defecto de ejemplo no es `root`.

`JWT_SECRET` debe configurarse en ambientes reales con un valor privado y suficientemente largo. El valor por defecto es solo para desarrollo local.

## Estructura general

```text
src/main/java/com/puravida
├── PuraVidaApplication.java
├── shared
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── web
└── modules
    ├── users
    ├── auth
    ├── business
    ├── menu
    ├── orders
    ├── sales
    ├── notifications
    ├── dashboard
    └── reports
```

Cada modulo queda preparado con:

- `domain/model`
- `domain/exception`
- `application/usecase`
- `application/dto`
- `application/port/in`
- `application/port/out`
- `infrastructure/persistence`
- `infrastructure/repository`
- `web/controller`

## Base de datos

La base de datos vive fuera de este backend, en `../pura-vida-database`.

No se ejecutan migraciones desde este esqueleto inicial.

No se persiste una tabla auxiliar de consolidacion; las ventas consolidadas se obtienen mediante consultas SQL/JOIN desde el backend.

## Endpoint tecnico

```text
GET /api/v1/health
```

Respuesta esperada:

```json
{
  "status": "OK",
  "project": "PuraVida"
}
```

## Autenticacion

El registro publico crea siempre usuarios con rol `cliente`. El rol `encargada` no se puede crear desde el endpoint publico de registro.

### Registro

```text
POST /api/v1/auth/register
```

Request:

```json
{
  "nombre": "Ana Perez",
  "correo": "ana@example.com",
  "telefono": "9610000000",
  "password": "password123"
}
```

Response:

```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "nombre": "Ana Perez",
    "correo": "ana@example.com",
    "telefono": "9610000000",
    "rol": "cliente",
    "iconoPerfil": null,
    "notificacionesActivas": true,
    "activo": true
  }
}
```

### Login

```text
POST /api/v1/auth/login
```

Request:

```json
{
  "correo": "ana@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "status": "OK",
  "data": {
    "token": "jwt-token",
    "tokenType": "Bearer",
    "expiresInMinutes": 120,
    "user": {
      "id": 1,
      "nombre": "Ana Perez",
      "correo": "ana@example.com",
      "telefono": "9610000000",
      "rol": "cliente",
      "iconoPerfil": null,
      "notificacionesActivas": true,
      "activo": true
    }
  }
}
```

Las respuestas no exponen `Password_hash`.

## Documentacion funcional

- [Autenticacion y perfil propio](docs/endpoints/auth-profile.md)
- [Configuracion publica del negocio](docs/endpoints/business-configuration.md)
- [Reportes semanales JSON y PDF](docs/endpoints/reports.md)
- [Demo E2E del MVP con Postman](docs/testing/mvp-e2e-postman.md)
- [Historial de pedidos del cliente](docs/endpoints/orders.md)
- [Carrito](docs/endpoints/cart.md)
- [Catalogo de platillos](docs/endpoints/dishes.md)
- [Menu diario](docs/endpoints/menu.md)
- [Coleccion Postman](docs/postman/PuraVida.postman_collection.json)
