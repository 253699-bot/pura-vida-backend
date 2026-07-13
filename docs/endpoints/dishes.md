# Catalogo de platillos

## Eliminar platillo

`DELETE /api/v1/admin/dishes/{id}`

Requiere `Authorization: Bearer <token>` de una usuaria activa con rol `encargada`.

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
