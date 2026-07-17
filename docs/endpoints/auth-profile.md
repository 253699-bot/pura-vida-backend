# Autenticacion y perfil propio

## Registrar cliente

`POST /api/v1/auth/register`

El registro es publico y siempre crea un usuario activo con rol `cliente`. El
request no acepta rol y el backend no toma esa decision desde datos enviados por
el cliente.

```json
{
  "nombre": "Ana Perez",
  "correo": "ana@example.com",
  "telefono": "9610000000",
  "password": "password123"
}
```

El correo se normaliza a minusculas, se valida contra el indice unico de
`USUARIOS.Correo` y la password se almacena exclusivamente como hash BCrypt.
La respuesta contiene el resumen del usuario, pero nunca `password`,
`passwordHash` ni `Password_hash`.

Errores:

- `400 Bad Request`: formato o longitudes invalidas.
- `409 Conflict`: el correo normalizado ya esta registrado o la restriccion
  unica detecta una carrera de registros simultaneos.

## Consultar mi perfil

`GET /api/v1/me`

Requiere `Authorization: Bearer <token>`. El backend toma el `userId` del JWT y
vuelve a consultar `USUARIOS`; no acepta un ID de usuario por URL, query o body.
La cuenta debe existir y permanecer activa.

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

## Actualizar mi perfil

`PATCH /api/v1/me`

Permite cambiar parcialmente `nombre` y `telefono`. Al omitir uno de ellos se
conserva su valor actual; enviar `telefono` vacio lo limpia.

```json
{
  "nombre": "Ana Maria",
  "telefono": "9611111111"
}
```

La actualización bloquea la fila del usuario autenticado y conserva siempre su
`correo`, `rol`, `passwordHash`, icono, preferencias y estado. Los campos
`correo`, `rol` y `password` se rechazan explícitamente con `400 Bad Request`;
el cambio de password queda fuera de este endpoint.

Errores de perfil:

- `400 Bad Request`: body vacio, nombre vacio, longitudes invalidas o intento
  de modificar campos protegidos.
- `401 Unauthorized`: bearer token ausente o invalido.
- `403 Forbidden`: el usuario del token no existe o esta inactivo.

Ejemplos manuales:

```bash
curl "$BASE_URL/api/v1/me" \
  -H "Authorization: Bearer $USER_TOKEN"

curl -X PATCH "$BASE_URL/api/v1/me" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Maria","telefono":"9611111111"}'
```
