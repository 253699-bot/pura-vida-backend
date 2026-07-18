# Notificaciones internas

Los endpoints requieren `Authorization: Bearer <token>` de un usuario activo.
Funcionan para clientes y encargadas. La identidad siempre se obtiene del JWT;
ningun endpoint acepta un `usuarioId` enviado por el cliente.

## Listar mis notificaciones

`GET /api/v1/notifications`

Devuelve exclusivamente las notificaciones del usuario autenticado, ordenadas
de la mas reciente a la mas antigua.

```json
{
  "status": "OK",
  "data": [
    {
      "id": 15,
      "pedidoId": 30,
      "tipo": "pedido_aceptado",
      "titulo": "Pedido aceptado",
      "mensaje": "Tu pedido #30 fue aceptado.",
      "leido": false,
      "fecha": "2026-07-17T13:05:00",
      "leidoEn": null
    }
  ]
}
```

## Marcar una notificacion como leida

`PATCH /api/v1/notifications/{id}/read`

Solo busca el identificador dentro de las notificaciones del usuario
autenticado. Si no existe o pertenece a otro usuario, responde `404` sin
revelar a quien pertenece. La operacion es idempotente.

## Marcar todas como leidas

`PATCH /api/v1/notifications/read-all`

Actualiza solo las notificaciones no leidas del usuario autenticado.

```json
{
  "status": "OK",
  "data": {
    "actualizadas": 3
  }
}
```

## Eventos generados

- `pedido_creado`: se crea para cada encargada activa con notificaciones
  habilitadas cuando un cliente crea un pedido.
- `pedido_aceptado`: se crea para el cliente cuando la encargada acepta el
  pedido.
- `pedido_rechazado`: se crea para el cliente cuando la encargada rechaza el
  pedido e incluye el motivo.
- `sistema`: reservado para registros historicos o notificaciones generales.

`USUARIOS.Notificaciones_act = false` evita nuevas notificaciones para ese
usuario, pero no elimina ni impide consultar notificaciones existentes.

## Seguridad y errores

- `401 Unauthorized`: falta el bearer token o no es valido.
- `403 Forbidden`: el usuario no existe o esta inactivo.
- `404 Not Found`: la notificacion no existe o pertenece a otro usuario.

```bash
curl "$BASE_URL/api/v1/notifications" \
  -H "Authorization: Bearer $TOKEN"

curl -X PATCH "$BASE_URL/api/v1/notifications/$NOTIFICATION_ID/read" \
  -H "Authorization: Bearer $TOKEN"

curl -X PATCH "$BASE_URL/api/v1/notifications/read-all" \
  -H "Authorization: Bearer $TOKEN"
```
