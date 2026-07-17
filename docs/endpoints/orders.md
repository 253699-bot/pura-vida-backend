# Historial de pedidos del cliente

Los endpoints requieren `Authorization: Bearer <token>` de un usuario activo con
rol `cliente`. La identidad se obtiene del JWT; el cliente no envia su ID en la
URL ni como parametro.

## Listar mis pedidos

`GET /api/v1/orders/my`

Devuelve exclusivamente los pedidos asociados al cliente autenticado, ordenados
por fecha y hora descendentes. Cada elemento incluye estado, fecha, hora, total y
datos operativos del pedido.

Respuesta abreviada:

```json
{
  "status": "OK",
  "data": [
    {
      "id": 10,
      "clienteId": 1,
      "clienteNombre": "Ana Perez",
      "estado": "pendiente",
      "fecha": "2026-07-17",
      "hora": "13:30:00",
      "total": 180.00,
      "notas": "Sin cebolla",
      "motivoRechazo": null,
      "respondidoPor": null,
      "respondidoEn": null
    }
  ]
}
```

## Consultar uno de mis pedidos

`GET /api/v1/orders/my/{id}`

Devuelve el encabezado del pedido y el detalle historico de platillos almacenado
en `DETALLE_PEDIDO`. El nombre, precio unitario y subtotal son los snapshots
guardados cuando se creo el pedido, por lo que no dependen de cambios posteriores
en el menu.

Respuesta abreviada:

```json
{
  "status": "OK",
  "data": {
    "id": 10,
    "estado": "pendiente",
    "fecha": "2026-07-17",
    "hora": "13:30:00",
    "total": 180.00,
    "items": [
      {
        "id": 30,
        "menuItemId": 20,
        "platilloId": 2,
        "nombre": "Comida corrida",
        "cantidad": 2,
        "precioUnitario": 90.00,
        "subtotal": 180.00
      }
    ]
  }
}
```

## Seguridad y errores

- `401 Unauthorized`: falta el bearer token o no es valido.
- `403 Forbidden`: el usuario no existe, esta inactivo o no tiene rol `cliente`.
- `404 Not Found`: el pedido no existe o pertenece a otro cliente. Se usa la
  misma respuesta para no revelar la existencia de pedidos ajenos.

Ejemplos manuales:

```bash
curl "$BASE_URL/api/v1/orders/my" \
  -H "Authorization: Bearer $CLIENT_TOKEN"

curl "$BASE_URL/api/v1/orders/my/$ORDER_ID" \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```
