# Despliegue Backend PuraVida en AWS EC2 con GitHub Actions

## Objetivo

Preparar un flujo manual y repetible para construir la API Spring Boot de PuraVida con Java 17, enviar su JAR a una instancia EC2 y ejecutarla con systemd. Esta configuración no crea infraestructura AWS ni ejecuta un despliegue hasta que el workflow se lance manualmente.

## Arquitectura del despliegue

1. GitHub Actions obtiene el código del backend, ejecuta las pruebas y construye el JAR.
2. El workflow selecciona el JAR ejecutable de `target/`, excluye archivos con sufijo `-plain.jar` si existieran y lo nombra `pura-vida-backend.jar`.
3. GitHub Actions lo sube por SSH/SCP a `/tmp` de EC2.
4. Un comando remoto lo instala en el directorio de la aplicación y reinicia systemd.
5. EC2 ejecuta la API como el usuario Linux `puravida`.

Las credenciales de GitHub y las variables de ejecución de la aplicación no se guardan en el repositorio.

## Archivos creados

- `.github/workflows/deploy-backend-ec2.yml`: workflow manual que prueba, construye y publica el JAR.
- `deploy/backend/deploy.sh`: instalación manual de un JAR que ya está en EC2.
- `deploy/backend/puravida-api.service.example`: plantilla de unidad systemd.
- `deploy/backend/puravida-api.env.example`: plantilla de variables de runtime.
- `deploy/backend/README.md`: uso breve de los recursos de despliegue.

## Variables y secretos

### GitHub Repository Secrets

Configura estos datos en GitHub: **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.

| Secret | Descripción |
| --- | --- |
| `EC2_HOST` | IP pública o DNS de la instancia EC2. |
| `EC2_USER` | Usuario SSH, por ejemplo `ubuntu` o `ec2-user`. Debe poder usar los comandos sudo documentados sin pedir contraseña. |
| `EC2_SSH_KEY` | Llave privada SSH completa del usuario configurado. GitHub la enmascara y el workflow la elimina al terminar. |
| `EC2_PORT` | Puerto SSH; normalmente `22`. Si se deja vacío, el workflow usa `22`. |
| `EC2_APP_DIR` | Directorio de instalación del JAR; normalmente `/opt/puravida-api`. |

No se requieren credenciales de API de AWS para este flujo: la entrega se realiza directamente por SSH.

### Variables de entorno en EC2

Estas variables son de runtime. No deben ir en GitHub Actions ni en el repositorio. Deben configurarse exclusivamente en `/etc/puravida-api/puravida-api.env`.

- `PURAVIDA_DB_URL`
- `PURAVIDA_DB_USERNAME`
- `PURAVIDA_DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`

Usa `deploy/backend/puravida-api.env.example` como plantilla. El archivo real debe pertenecer a `root:puravida` y tener permisos `0640` para que systemd pueda leerlo sin hacerlo público.

## Preparar EC2

Los siguientes comandos son orientativos para Ubuntu. Ejecútalos directamente en EC2 con los valores reales solo cuando la instancia esté preparada; no forman parte de este repositorio.

### Sistema y Java 17

~~~bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless
java -version
~~~

Para Amazon Linux, instala la distribución de Java 17 equivalente disponible para esa imagen.

### Usuario y directorios

~~~bash
sudo useradd --system --home-dir /opt/puravida-api --shell /usr/sbin/nologin puravida
sudo install -d -o puravida -g puravida -m 0755 /opt/puravida-api
sudo install -d -o root -g puravida -m 0750 /etc/puravida-api
~~~

Si el usuario ya existe, no vuelvas a crearlo.

### Archivo de entorno

~~~bash
sudo cp deploy/backend/puravida-api.env.example /etc/puravida-api/puravida-api.env
sudo chown root:puravida /etc/puravida-api/puravida-api.env
sudo chmod 0640 /etc/puravida-api/puravida-api.env
sudoedit /etc/puravida-api/puravida-api.env
~~~

Reemplaza cada placeholder antes de iniciar la API. No copies el archivo real de entorno de una máquina a otra por canales inseguros.

### Unidad systemd

~~~bash
sudo cp deploy/backend/puravida-api.service.example /etc/systemd/system/puravida-api.service
sudo systemctl daemon-reload
sudo systemctl enable puravida-api
sudo systemctl start puravida-api
~~~

La unidad espera el archivo `/opt/puravida-api/pura-vida-backend.jar`. El primer inicio debe ocurrir después de subir e instalar el JAR.

### Permisos para el usuario usado por GitHub Actions

El workflow carga el JAR en `/tmp` y usa `sudo -n` para instalarlo y controlar el servicio. Configura para `EC2_USER` permisos sudo sin contraseña y de mínimo privilegio para los comandos necesarios: `install`, `rm`, `systemctl daemon-reload`, `systemctl restart puravida-api` y `systemctl status puravida-api --no-pager`. Revisa esta regla con el responsable de seguridad antes de aplicarla.

## Configurar seguridad

- No ejecutes la aplicación como `root`; la unidad la ejecuta como `puravida`.
- No subas archivos `.env`, llaves `.pem`, llaves privadas SSH ni credenciales al repositorio.
- Abre únicamente los puertos necesarios. El puerto 22 debe restringirse por IP cuando sea posible.
- El puerto 8080 puede abrirse temporalmente para pruebas; para producción es preferible exponer HTTPS mediante un reverse proxy.
- Configura los Security Groups de AWS con el mínimo acceso necesario.
- Usa un `JWT_SECRET` largo, aleatorio y privado.
- Usa un usuario MySQL específico para la aplicación; nunca `root`.
- La huella SSH del host debe verificarse antes de habilitar el workflow. El workflow usa `ssh-keyscan` en un runner efímero; en producción conviene reemplazarlo por una clave de host conocida y verificada.

## Ejecutar deployment

1. Confirma que los cinco secrets de GitHub están configurados y que EC2 se preparó.
2. En el repositorio de GitHub, abre la pestaña **Actions**.
3. Selecciona **Deploy Backend to AWS EC2**.
4. Pulsa **Run workflow** y confirma la rama que se desplegará.
5. Revisa los logs de pruebas, construcción, copia y estado de systemd.

El workflow solo tiene `workflow_dispatch`. El bloque comentado de `push` para `dev/aiko` puede habilitarse posteriormente tras validar el proceso manual.

El build que genera el JAR es:

~~~bash
mvn clean package -DskipTests
~~~

## Validar despliegue

En una terminal con acceso a la instancia, comprueba:

~~~bash
curl http://IP_PUBLICA:8080/api/v1/health
sudo systemctl status puravida-api
journalctl -u puravida-api -f
~~~

Una respuesta correcta de salud y un servicio `active (running)` confirman que el proceso quedó activo. Si se coloca Nginx delante de la API, valida también la URL pública HTTPS.

## Pendientes antes de producción

- Dominio y DNS.
- HTTPS y certificados.
- Reverse proxy Nginx.
- Base de datos RDS o una instalación MySQL segura.
- Backups verificables.
- Centralización y retención de logs.
- Monitoreo y alertas.
- CORS de producción.
- Perfiles Spring separados para desarrollo y producción.
- Rotación de secrets.
- Pipeline separado por ambientes.

## Troubleshooting

### Java no instalado

Si systemd muestra que no existe `/usr/bin/java` o la versión no es 17, instala Java 17 y comprueba `java -version`. Ajusta `ExecStart` solo si la ruta de Java difiere en la imagen elegida.

### Puerto cerrado en Security Group

Si el JAR está activo pero `curl` remoto falla, confirma que el proceso escucha en 8080 y revisa firewall local, Security Group y reglas de red. No abras 8080 públicamente si habrá reverse proxy.

### El servicio no arranca

Ejecuta `sudo systemctl status puravida-api --no-pager` y `journalctl -u puravida-api -n 100 --no-pager`. Comprueba la ruta del JAR, el archivo de entorno y que el usuario `puravida` pueda leer el JAR.

### Error de conexión a MySQL

Revisa `PURAVIDA_DB_URL`, usuario, contraseña, alcance de red y reglas de acceso de MySQL. Confirma también que el usuario de MySQL tiene únicamente los permisos necesarios para PuraVida.

### JWT_SECRET no configurado

Confirma que `/etc/puravida-api/puravida-api.env` existe, tiene una línea `JWT_SECRET` no vacía, permisos correctos y que se reinició el servicio después de editarlo.

### JAR no encontrado

El workflow espera un JAR ejecutable en `target/` y excluye `*-plain.jar`. En EC2, la unidad espera exactamente `/opt/puravida-api/pura-vida-backend.jar`; vuelve a ejecutar la instalación si falta.

### Permisos incorrectos en `/opt/puravida-api`

Verifica `ls -ld /opt/puravida-api` y `ls -l /opt/puravida-api/pura-vida-backend.jar`. Ambos deben permitir al usuario y grupo `puravida` acceder al JAR. Reinstala con `deploy/backend/deploy.sh` si el propietario o modo no coincide.
