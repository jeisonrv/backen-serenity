# Cuentas institucionales de Serenity

El backend crea las cuentas institucionales al iniciar, después de que Flyway y la
conexión a PostgreSQL estén disponibles. El seeder utiliza `PasswordEncoder` de Spring Security (BCrypt), guarda solo el hash y usa
inserciones `ON CONFLICT DO NOTHING`; puede ejecutarse en varios reinicios o
réplicas sin duplicar la cuenta.

| Correo | Rol |
|---|---|
| `oyente.serenity@example.com` | `OYENTE` |
| `moderador.serenity@example.com` | `MODERADOR` |

En una cuenta existente nunca se cambia la contraseña. Si su rol no coincide con
el rol institucional, el seeder corrige únicamente ese rol y conserva el resto de
sus datos. Para una cuenta inexistente, configure las contraseñas iniciales como
secretos de despliegue:

| Variable | Contenido |
|---|---|
| `SERENITY_SEED_OYENTE_PASSWORD` | Contraseña inicial de la cuenta OYENTE |
| `SERENITY_SEED_MODERADOR_PASSWORD` | Contraseña inicial de la cuenta MODERADOR |

No escriba contraseñas en archivos versionados ni en scripts de despliegue. Los
valores de prueba compartidos durante el desarrollo no deben reutilizarse como
contraseñas de producción; genere secretos nuevos y almacénelos en Azure Key Vault.
El seeder no registra sus valores. Si falta una variable, una cuenta nueva de ese
rol no se crea y se emite una advertencia sin incluir la contraseña.

## Azure App Service

Configure en **App Service → Configuration → Application settings** (preferiblemente
mediante referencias a Key Vault):

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL=jdbc:postgresql://<host-neon>:5432/<base>?sslmode=require`
- `DB_USER` y `DB_PASSWORD` para la base de producción de Neon
- `JWT_SECRET` como secreto aleatorio de al menos 32 bytes y `JWT_EXPIRATION`
- `SERENITY_SEED_OYENTE_PASSWORD`
- `SERENITY_SEED_MODERADOR_PASSWORD`

Los perfiles `prod` y base comparten `DB_URL`; `prod` configura Hibernate para
validar el esquema. Confirme que las App Settings de Azure apuntan al mismo Neon
que se inspecciona, reinicie el App Service y revise los logs de inicio. El
seeder debe indicar que cada cuenta fue creada o que ya existe; la dirección se
enmascara y las contraseñas nunca se escriben en logs.

Después del despliegue, pruebe `POST /api/auth/login` por HTTPS para cada cuenta
con las credenciales gestionadas de forma segura. Verifique `usuario.id`,
`usuario.email`, `usuario.rol` y la claim `rol` del JWT; no copie ni publique el
token. Compruebe también que una cuenta OYENTE recibe `403` en `/api/moderacion/**`
y una cuenta MODERADOR recibe `403` en `/api/oyente/**`. No se debe cambiar la
configuración de seguridad ni el secreto JWT para resolver errores de credenciales.
