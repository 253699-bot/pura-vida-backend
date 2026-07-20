# Reportes semanales

Los reportes requieren `Authorization: Bearer <token>` de una usuaria activa
con rol `encargada`. Los importes se calculan desde `VENTAS`; los pedidos y el
top de platillos reutilizan las métricas de dashboard para el mismo rango.

## Resumen JSON

`GET /api/v1/admin/reports/weekly/summary?weekStart=YYYY-MM-DD`

Devuelve el rango semanal, ventas activas/anuladas, desglose por fuente,
pedidos operativos y top de platillos. `weekStart` es opcional; si se omite se
usa el lunes de la semana actual. Cuando se envía debe usar formato ISO
`YYYY-MM-DD`.

## Descargar PDF

`GET /api/v1/admin/reports/weekly/pdf?weekStart=YYYY-MM-DD`

Este endpoint ya genera un PDF real mediante PDFBox a partir del mismo resumen
semanal. No devuelve un envelope JSON.

Respuesta exitosa:

- `200 OK`.
- `Content-Type: application/pdf`.
- `Content-Disposition: attachment; filename="reporte-semanal-puravida-YYYY-MM-DD.pdf"`.
- `Content-Length` con el tamaño del binario.
- El body inicia con la firma `%PDF-`.

El documento incluye resumen de ventas, ventas por fuente, estados de pedidos,
top de platillos y notas sobre anulaciones y ventas manuales.

## Persistir un reporte semanal

`POST /api/v1/admin/reports/weekly`

```json
{
  "weekStart": "2026-07-06"
}
```

`weekStart` es obligatorio y debe corresponder a un lunes. El endpoint calcula
el resumen una sola vez y persiste un snapshot JSON con `Version_formato=1`.
`Ruta_archivo` y `Dia_mayor_demanda` permanecen `NULL`; el platillo mas vendido
solo se guarda cuando el resumen calculado contiene un ID real.

La operacion es idempotente por `(Semana_inicio, Semana_fin)`. Si la semana ya
existe, devuelve el mismo registro sin recalcular ni sobrescribir su historia.

## Listar reportes persistidos

`GET /api/v1/admin/reports/weekly`

Devuelve metadatos ordenados por semana descendente. El campo
`snapshotDisponible` identifica filas historicas anteriores a la persistencia
JSON sin exponer el snapshot completo.

## Descargar PDF historico por ID

`GET /api/v1/admin/reports/weekly/{id}/pdf`

Deserializa el snapshot almacenado y reutiliza el mismo generador PDFBox. No
consulta metricas actuales y no lee `Ruta_archivo`. Una fila sin snapshot, con
version no soportada, JSON corrupto o rango inconsistente responde `409
Conflict`; nunca se recalcula ni reescribe silenciosamente.

Errores:

- `400 Bad Request`: `weekStart` no tiene formato `YYYY-MM-DD`.
- `401 Unauthorized`: falta el bearer token o no es válido.
- `403 Forbidden`: la cuenta no existe, está inactiva o no tiene rol
  `encargada`.
- `404 Not Found`: el ID de reporte solicitado no existe.
- `409 Conflict`: el registro historico no tiene un snapshot compatible.

Ejemplos:

```bash
curl "$BASE_URL/api/v1/admin/reports/weekly/summary?weekStart=$WEEK_START" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

curl "$BASE_URL/api/v1/admin/reports/weekly/pdf?weekStart=$WEEK_START" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  --output "reporte-semanal-$WEEK_START.pdf"
```

La colección Postman verifica status, headers y firma del PDF. Para conservar
el archivo desde Postman se puede usar **Send and Download** sobre la petición
`Descargar reporte semanal PDF`.
