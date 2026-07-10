# Recursos de despliegue del backend

Esta carpeta contiene plantillas y un script manual para instalar el JAR de PuraVida en una instancia Linux. Ninguno de estos archivos hace un despliegue por sí mismo ni contiene secretos.

## Contenido

- `deploy.sh`: instala un JAR ya cargado en el servidor y reinicia el servicio.
- `puravida-api.service.example`: plantilla de unidad systemd.
- `puravida-api.env.example`: plantilla de variables de ejecución para EC2.

## Preparar systemd

Copia `puravida-api.service.example` como `/etc/systemd/system/puravida-api.service`. Antes de activarlo, confirma que existe el usuario y grupo de sistema `puravida`, el directorio `/opt/puravida-api` y el JAR esperado.

~~~bash
sudo cp deploy/backend/puravida-api.service.example /etc/systemd/system/puravida-api.service
sudo systemctl daemon-reload
sudo systemctl enable puravida-api
~~~

## Crear el archivo de entorno en EC2

Copia la plantilla a `/etc/puravida-api/puravida-api.env`, reemplaza todos los placeholders con valores de producción y limita sus permisos. Ese archivo permanece únicamente en EC2.

~~~bash
sudo install -d -m 0750 -o root -g puravida /etc/puravida-api
sudo cp deploy/backend/puravida-api.env.example /etc/puravida-api/puravida-api.env
sudo chmod 0640 /etc/puravida-api/puravida-api.env
sudo chown root:puravida /etc/puravida-api/puravida-api.env
sudoedit /etc/puravida-api/puravida-api.env
~~~

## Instalar un JAR manualmente

Después de subir el archivo al servidor, por ejemplo a `/tmp/pura-vida-backend.jar`, ejecuta el script desde el repositorio o una copia confiable de él:

~~~bash
chmod +x deploy/backend/deploy.sh
sudo deploy/backend/deploy.sh /tmp/pura-vida-backend.jar
~~~

El script crea `/opt/puravida-api` si hace falta, instala el JAR con propietario `puravida`, recarga systemd, reinicia el servicio y muestra su estado.

Para una operación posterior:

~~~bash
sudo systemctl restart puravida-api
sudo systemctl status puravida-api --no-pager
~~~

## No subir al repositorio

No subas el archivo real `/etc/puravida-api/puravida-api.env`, archivos `.env`, llaves privadas SSH o `.pem`, tokens, credenciales de MySQL ni JARs generados.
