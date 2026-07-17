# Demo E2E del MVP con Postman

La colección `docs/postman/PuraVida.postman_collection.json` contiene el folder
`Demo E2E MVP - pedido aceptado`. Está ordenado para ejecutarse completo con el
Collection Runner y no contiene tokens ni credenciales reales.

## Prerrequisitos

- Backend y MySQL de prueba disponibles; no usar producción.
- Fonda configurada como abierta para el día actual.
- Menú del día configurado con al menos un platillo disponible.
- Una cuenta activa `cliente` y una cuenta activa `encargada`.
- Configurar localmente las variables `clientEmail`, `clientPassword`,
  `adminEmail` y `adminPassword`.

`weekStart` se calcula automáticamente como el lunes de la semana actual si la
variable está vacía. También puede fijarse manualmente en formato `YYYY-MM-DD`.

## Secuencia automatizada

1. Login cliente; guarda `clientToken`.
2. Consulta menú de hoy; selecciona el primer platillo disponible y guarda
   `dishId` desde `platilloId`.
3. Agrega el platillo al carrito con `quantity`.
4. Ejecuta checkout; guarda el `orderId` del pedido pendiente.
5. Login encargada; guarda `adminToken`.
6. Acepta el pedido capturado.
7. Consulta ventas remotas activas y valida la venta asociada al `orderId`.
8. Consulta dashboard de hoy.
9. Consulta resumen semanal JSON.
10. Descarga y valida el reporte semanal PDF.

Los tests Postman detienen conceptualmente la demo cuando falta el menú, el
pedido no queda pendiente, la aceptación no produce estado `aceptado`, no se
encuentra la venta remota o el PDF no responde con firma `%PDF-`.

## Ruta alternativa de rechazo

Aceptar y rechazar son transiciones excluyentes. Para demostrar rechazo se debe
crear un segundo pedido pendiente, guardar su ID en `rejectedOrderId` y ejecutar
manualmente `Administracion de pedidos / Rechazar pedido alternativo`. No se
debe reutilizar el `orderId` ya aceptado por la demo principal.

## Evidencias esperadas

- Pedido creado en estado `pendiente` y después `aceptado`.
- Venta `remota` y `activa` asociada al pedido.
- Dashboard y resumen semanal accesibles para la encargada.
- Respuesta PDF con `Content-Type: application/pdf`, header de descarga y body
  iniciado por `%PDF-`.

La colección valida contratos HTTP y encadena IDs/tokens, pero los resultados
de negocio dependen de los datos de la instancia de prueba. No se ejecutó esta
demo contra DB real ni EC2 durante esta entrega.
