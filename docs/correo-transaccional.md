# Correo transaccional

El backend usa Resend para enviar un correo de bienvenida al crear una cuenta con correo y contraseña o al crear una cuenta nueva desde Google. La vinculación de Google y los inicios de sesión posteriores no envían correos.

El correo está desactivado por defecto. Si Resend no está configurado o el envío falla, el registro continúa y el error no expone información del proveedor al usuario.

## Configuración

1. Crea una cuenta en Resend y genera una API key para el backend.
2. Verifica un dominio remitente en Resend y usa una dirección de ese dominio como remitente.
3. En el `.env` que usa Docker Compose, configura:

   ```env
   EMAIL_ENABLED=true
   RESEND_API_KEY=re_tu_api_key
   RESEND_FROM=Serenity <no-reply@tu-dominio-verificado.com>
   ```

4. Recrea el backend para cargar las variables:

   ```powershell
   docker compose up -d --build backend
   ```

La API key es un secreto del backend: no la agregues al frontend ni al repositorio. El Client ID de Google no se usa para enviar correos. El backend hace un `POST https://api.resend.com/emails` con autenticación Bearer y el contenido del mensaje.

## Comprobación

Registra una cuenta nueva o inicia por primera vez con una cuenta Google nueva. Revisa el buzón del destinatario y los logs de Resend. Si el envío falla, el usuario igualmente podrá entrar a Serenity; revisa que la API key y el remitente correspondan al dominio verificado.
