# Menu diario

## Consultar menu publicado

`GET /api/v1/menu/today`

Es publico. Devuelve exclusivamente filas de hoy con `MENU_DIA.Publicado =
true` cuyo platillo sigue activo. Cada item informa su disponibilidad. Cuando
no hay filas publicadas devuelve `configured=false` y una lista vacia.

## Publicar o actualizar menu

`PUT /api/v1/menu/today`

Requiere rol `encargada`.

```json
{
  "items": [
    { "platilloId": 10 },
    { "platilloId": 12 }
  ]
}
```

No permite IDs invalidos, duplicados, inexistentes ni platillos inactivos. La
lista vacia es valida y retira todos los items publicados. La operacion conserva
las filas historicas: los omitidos cambian a `Publicado=false`; los que vuelven
a seleccionarse se reactivan y recuperan el precio base vigente. Nunca elimina
`MENU_DIA` ni `DISPONIBILIDAD_MENU`.

## Cambiar disponibilidad

`PATCH /api/v1/menu/today/items/{id}/availability`

Requiere rol `encargada` y un item perteneciente al menu de hoy.

```json
{ "disponible": false }
```

Un item no disponible conserva historial y puede seguir representado en el
panel administrativo, pero no puede agregarse al carrito, convertirse en pedido
ni registrarse en una venta manual nueva.
