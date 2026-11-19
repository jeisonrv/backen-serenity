# Pruebas unitarias y E2E para Serenity

Este paquete está adaptado a la estructura revisada del backend `jeisonrv/backen-serenity` (rama `preproduccion`) y al login del frontend Angular `jeisonrv/frontend-serenity` (rama `feature/jardin-interactivo-animado`). Las pruebas unitarias usan JUnit 5 y Mockito, que ya llegan mediante `spring-boot-starter-test`. La prueba E2E usa Selenium Java.

## 1. Instalar los archivos de prueba

1. Descomprime el ZIP dentro de la carpeta raíz `backen-serenity`.
2. Copia/combina la carpeta `../test` del ZIP con `backen-serenity/src/test`. Si ya tienes pruebas, no borres las existentes.
3. Abre `backen-serenity/pom.xml` y agrega el contenido de `pom-selenium-dependency.xml` dentro de `<dependencies>`, antes del cierre `</dependencies>`. No pegues el comentario ni un segundo bloque `<dependencies>` si no corresponde.
4. Guarda el `../../pom.xml`.

## 2. Qué prueban las unitarias

- `EstadoAnimoServiceTest`: registra y actualiza el ánimo, conserva ID/fecha original y rechaza valores inválidos y rangos de días inválidos.
- `CompletarSesionServiceTest`: guarda una sesión, calcula XP según el comportamiento actual y actualiza el progreso; rechaza datos inválidos.
- `DiarioServiceTest`: guarda entradas, usa el prompt `libre` por defecto, valida contenido/título, conserva título/prompt al editar y evita obtener entradas de otro usuario.

Estas pruebas usan mocks para que no necesiten una conexión real a PostgreSQL.

## 3. Ejecutar las unitarias en Windows PowerShell

Desde `backen-serenity`:

```powershell
.\mvnw.cmd test
```

También puedes ejecutar una clase específica:

```powershell
.\mvnw.cmd -Dtest=EstadoAnimoServiceTest test
.\mvnw.cmd -Dtest=CompletarSesionServiceTest test
.\mvnw.cmd -Dtest=DiarioServiceTest test
```

Busca `BUILD SUCCESS` y el resumen `Tests run: ... Failures: 0, Errors: 0`.

## 4. Preparar y ejecutar las E2E

La prueba E2E necesita que el frontend esté levantado en `http://localhost:8080` y que Chrome esté instalado. La prueba de login completo, además, necesita una cuenta **de prueba** de Serenity.

Primero comprueba que Docker no esté reiniciando el frontend. Desde `backen-serenity`:

```powershell
docker compose up -d
 docker compose ps
```

Si el frontend aparece `Restarting`, soluciona ese problema primero. La prueba E2E no corrige fallos de Docker.

Para probar el formulario y sus validaciones sin credenciales, ejecuta:

```powershell
.\mvnw.cmd -Dtest=SerenityE2ESmoke test
```

La clase `SerenityE2ESmoke` se ejecuta explícitamente y no se incluye en la ejecución unitaria normal (`mvnw.cmd test`). Selenium Manager intenta encontrar o descargar el controlador compatible del navegador; en el primer uso puede necesitar conexión a Internet.

### Flujo de login de extremo a extremo (opcional, requiere cuenta de prueba)

En PowerShell, define credenciales de un usuario de prueba existente. No las escribas en el código ni las compartas en capturas:

```powershell
$env:SERENITY_E2E_EMAIL = "correo-de-prueba@example.com"
$env:SERENITY_E2E_PASSWORD = "CONTRASENA_DE_PRUEBA"
.\mvnw.cmd -Dtest=SerenityE2ESmoke test
Remove-Item Env:SERENITY_E2E_EMAIL
Remove-Item Env:SERENITY_E2E_PASSWORD
```

Si no defines estas variables, ese método se marca como omitido automáticamente; las otras dos pruebas E2E siguen ejecutándose. Para ver Chrome en pantalla en vez de modo headless:

```powershell
$env:SERENITY_E2E_HEADLESS = "false"
.\mvnw.cmd -Dtest=SerenityE2ESmoke test
Remove-Item Env:SERENITY_E2E_HEADLESS
```

## 5. Alcance y notas

- Las unitarias son pruebas de servicios del backend con dependencias simuladas; no modifican ni borran datos de PostgreSQL.
- La E2E comprueba la pantalla de login, validación de campos y, si hay credenciales de prueba, el ingreso hasta el dashboard.
- No se incluyen contraseñas de Serenity en los archivos.
- Los archivos se prepararon a partir de la estructura de código revisada, pero no se pudieron ejecutar contra tu instalación local desde aquí. Si una prueba falla al compilar, comparte el error completo (sin secretos) para ajustar cualquier diferencia de tu copia local.
