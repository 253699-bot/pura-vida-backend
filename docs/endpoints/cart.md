# Carrito

El carrito contiene informacion temporal previa a confirmar un pedido. Cada
operacion requiere `Authorization: Bearer <token>` y solo permite operar los
items del usuario autenticado.

## Ver carrito

`GET /api/v1/cart`

Devuelve los items propios, subtotales y total calculado con el precio snapshot
guardado al agregar cada platillo.

## Agregar item

`POST /api/v1/cart/items`

```json
{
  "dishId": 2,
  "cantidad": 1
}
```

El platillo debe existir y estar activo. Si ya existe en el carrito del mismo
usuario, se incrementa la cantidad y se conserva el precio snapshot original.

## Actualizar cantidad

`PATCH /api/v1/cart/items/{cartItemId}`

```json
{
  "cantidad": 3
}
```

La cantidad debe ser positiva y el item debe pertenecer al usuario autenticado.

## Eliminar item

`DELETE /api/v1/cart/items/{cartItemId}`

Devuelve `204 No Content`. Elimina fisicamente la fila de `CARRITO_ITEMS` solo
despues de validar su propiedad. Esto es seguro porque el carrito no representa
un pedido confirmado ni datos de ventas o historial financiero.

Errores habituales: `401` sin token, `403` si el item pertenece a otro usuario,
`404` si el item no existe y `400` para cantidades invalidas.

Los platillos usan baja logica por sus relaciones historicas; los items del
carrito no comparten esa necesidad y por eso su eliminacion es fisica.
