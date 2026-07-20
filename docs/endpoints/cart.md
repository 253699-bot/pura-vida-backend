# Carrito

El carrito contiene informacion temporal previa a confirmar un pedido. Cada
operacion requiere `Authorization: Bearer <token>` y solo permite operar los
items del usuario autenticado con rol `cliente`.

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

La fonda debe estar abierta y el platillo debe pertenecer al menu publicado de
hoy, seguir activo y estar disponible. Si ya existe en el carrito del mismo
cliente, se incrementa la cantidad. El snapshot se actualiza al precio diario
vigente.

## Actualizar cantidad

`PATCH /api/v1/cart/items/{cartItemId}`

```json
{
  "cantidad": 3
}
```

La cantidad debe ser positiva, el item debe pertenecer al cliente autenticado y
el platillo debe seguir vendible bajo las mismas reglas del alta. La consulta se
limita por `(cartItemId, userId)`, por lo que un item ajeno tampoco revela su
existencia.

## Eliminar item

`DELETE /api/v1/cart/items/{cartItemId}`

Devuelve `204 No Content`. Elimina fisicamente la fila de `CARRITO_ITEMS` solo
despues de validar su propiedad. Esto es seguro porque el carrito no representa
un pedido confirmado ni datos de ventas o historial financiero.

Errores habituales: `401` sin token, `403` si el usuario no es cliente, `404`
si el item no existe o es ajeno, `400` para cantidades invalidas y `409` cuando
la fonda esta cerrada o el platillo ya no esta disponible.

Los platillos usan baja logica por sus relaciones historicas; los items del
carrito no comparten esa necesidad y por eso su eliminacion es fisica.

## Confirmar carrito

`POST /api/v1/cart/checkout`

No requiere body. Convierte el carrito del cliente autenticado en un pedido
`pendiente` con su `DETALLE_PEDIDO` y devuelve el pedido creado con sus items.

Antes de crear el pedido se valida que:

- el usuario autenticado tenga rol `cliente` y este activo;
- el carrito no este vacio;
- la fonda este abierta;
- cada platillo pertenezca al menu publicado de hoy, siga activo y este disponible.

Los precios y totales se recalculan con el precio vigente del menu diario; no
se confia en el precio snapshot del carrito. El pedido y su detalle se guardan
en la misma transaccion y `CARRITO_ITEMS` se vacia fisicamente solo despues de
crear el pedido correctamente. Si cualquier validacion o guardado falla, el
carrito se conserva.

Respuesta exitosa abreviada:

```json
{
  "status": "OK",
  "data": {
    "id": 10,
    "clienteId": 1,
    "estado": "pendiente",
    "total": 180.00,
    "items": [
      {
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

Errores habituales: `401` sin token, `403` para un usuario que no sea cliente
y `409` si el carrito esta vacio, la fonda esta cerrada, no existe menu para el
dia o algun platillo ya no esta disponible.
