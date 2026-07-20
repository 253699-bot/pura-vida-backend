# Catalogo de platillos

Todos los endpoints administrativos requieren `Authorization: Bearer <token>`
de una usuaria activa con rol `encargada`.

## Listar platillos activos

`GET /api/v1/admin/dishes`

## Crear platillo

`POST /api/v1/admin/dishes`

## Editar platillo

`PUT /api/v1/admin/dishes/{id}`

Crear y editar aceptan los mismos campos respaldados por el catalogo:

```json
{
  "nombre": "Comida corrida",
  "descripcion": "Incluye sopa y guisado",
  "tipoPlatillo": "platillo_fuerte",
  "precioBase": 85.00
}
```

El nombre no puede exceder 150 caracteres, el tipo debe pertenecer al enum
existente y el precio debe ser mayor a cero. Un platillo retirado no puede
editarse (`409 Conflict`).

## Eliminar platillo

`DELETE /api/v1/admin/dishes/{id}`

La eliminacion es logica: el registro se conserva y su columna `Activo` se actualiza a `false`. El platillo deja de aparecer en el listado administrativo de activos y no puede asignarse al menu del dia.

Respuesta exitosa: `200 OK` con el platillo actualizado dentro de la respuesta estandar de la API.

Errores comunes:

* `404 Not Found`: el platillo no existe.
* `409 Conflict`: el platillo ya estaba eliminado.
* `401 Unauthorized` o `403 Forbidden`: falta autorizacion o no corresponde al rol `encargada`.

Ejemplo:

```bash
curl -X DELETE "$BASE_URL/admin/dishes/$DISH_ID" \
  -H "Authorization: Bearer $ENCARGADA_BEARER_TOKEN"
```
