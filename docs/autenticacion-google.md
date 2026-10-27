# Inicio de sesión con Google

El backend recibe el ID token emitido por Google Identity Services y lo valida en el servidor. Si el correo verificado no tiene cuenta, crea una cuenta con rol `USUARIO`; después devuelve el mismo `LoginResponse` (`token` de Serenity y `usuario`) que el login actual. Las contraseñas no se reciben ni se guardan para cuentas creadas por Google.

## Configurar Google Cloud

1. En Google Cloud Console, configura la pantalla de consentimiento OAuth y crea un cliente OAuth de tipo **Aplicación web**.
2. Añade los orígenes donde se sirve Angular, por ejemplo `http://localhost:4200` y `http://localhost:8080`, como orígenes autorizados de JavaScript.
3. Copia el **Client ID** web. Este flujo no necesita exponer un client secret al navegador ni al backend.
4. Agrega `GOOGLE_CLIENT_ID=tu-client-id-web.apps.googleusercontent.com` al archivo `.env` local.
5. Reconstruye y reinicia el backend con `docker compose up -d --build backend`.

El valor de `GOOGLE_CLIENT_ID` también se puede proporcionar como variable de entorno del proceso.

## Contrato de la API

El frontend debe mostrar el botón oficial de Google Identity Services. Cuando reciba el campo `credential`, debe enviarlo sin decodificar al backend:

```http
POST /api/auth/google
Content-Type: application/json

{"credential":"<ID token de Google>"}
```

La respuesta exitosa usa el contrato habitual de login:

```json
{
  "token": "<JWT de Serenity>",
  "usuario": {
    "id": 1,
    "email": "persona@example.com",
    "rol": "USUARIO"
  }
}
```

El cliente debe usar el JWT de Serenity para las llamadas posteriores a la API. No debe usar el ID token de Google como token de acceso de Serenity.

## Cuentas que ya existen

El backend no vincula automáticamente un Google ID a una cuenta local coincidente por correo. Para evitar asociar identidades solo por una dirección de correo, la persona debe iniciar sesión primero con su cuenta actual y luego enviar el ID token con su JWT:

```http
POST /api/auth/google/vincular
Authorization: Bearer <JWT de Serenity>
Content-Type: application/json

{"credential":"<ID token de Google>"}
```

La operación devuelve `204 No Content`. A partir de entonces, `POST /api/auth/google` reconoce el `sub` de Google asociado. Un mismo `sub` no se puede vincular a más de una cuenta.

## Errores esperados

- `400`: falta el campo `credential` o excede el tamaño máximo.
- `401`: el ID token es inválido, expiró o no tiene un correo verificado.
- `409`: ya existe una cuenta local con el correo; inicia sesión y vincula Google primero, o el ID de Google ya está vinculado a otro usuario.
- `503`: falta configurar `GOOGLE_CLIENT_ID` o Google no está disponible temporalmente.
