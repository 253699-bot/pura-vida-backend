# Configuracion publica del negocio

La configuracion vive en una unica fila de `CONFIGURACION_NEGOCIO`. El backend
no crea una configuracion alternativa en memoria: si el singleton no existe,
la API responde `404` para hacer visible el problema de despliegue.

## Consultar configuracion publica

`GET /api/v1/business/configuration`

No requiere autenticacion. Devuelve exclusivamente:

```json
{
  "status": "OK",
  "data": {
    "nombreFonda": "PuraVida",
    "logoUrl": "/api/v1/business/configuration/logo",
    "direccion": "Calle Central",
    "horarios": "Lunes a viernes",
    "telefono": "9610000000",
    "correo": "contacto@example.com",
    "actualizadoEn": "2026-07-18T10:00:00"
  }
}
```

`logoUrl` es `null` mientras no haya un logo almacenado. Esto permite al
frontend mantener su imagen fallback sin conocer rutas del servidor.

## Consultar logo actual

`GET /api/v1/business/configuration/logo`

No requiere autenticacion. Devuelve el binario PNG o JPEG actual con
`Content-Type`, `Content-Length`, `ETag`, cache publica y
`X-Content-Type-Options: nosniff`. No acepta nombres ni rutas como parametros.

## Actualizar datos publicos

`PATCH /api/v1/admin/business/configuration`

Requiere bearer token de una usuaria activa con rol `encargada`. Es un PATCH
parcial de `nombreFonda`, `direccion`, `horarios`, `telefono` y `correo`.
`logoUrl` no es editable mediante JSON.

```json
{
  "direccion": "Calle Central 10",
  "telefono": "+52 961 000 0000",
  "correo": "contacto@example.com"
}
```

## Subir logo

`POST /api/v1/admin/business/configuration/logo`

Requiere bearer token de encargada y `multipart/form-data` con un campo
`file`. Solo acepta PNG/JPEG reales de hasta 2 MiB. El backend verifica MIME y
contenido mediante ImageIO, limita dimensiones, ignora el nombre original,
genera una clave UUID y comprueba que la ruta normalizada permanezca dentro de
`PURAVIDA_LOGO_STORAGE_ROOT`.

Configuracion local:

- `PURAVIDA_LOGO_STORAGE_ROOT`: directorio relativo o volumen persistente.
- `PURAVIDA_LOGO_MAX_BYTES`: limite; por defecto `2097152`.

Produccion debe montar un volumen durable con permisos de escritura. Nunca se
expone la ruta fisica ni se almacena un nombre proporcionado por el cliente.

Errores comunes:

- `400`: PATCH vacio, validacion o archivo invalido.
- `401`: token ausente o invalido.
- `403`: cuenta inexistente, inactiva o sin rol `encargada`.
- `404`: singleton o logo no disponible.
