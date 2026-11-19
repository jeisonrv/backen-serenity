# Principios SOLID en Serenity Backend

El backend organiza sus funcionalidades por dominio y separa reglas de aplicación de los detalles de infraestructura. La arquitectura hexagonal ayuda a aplicar SOLID mediante casos de uso y puertos; hay áreas donde la separación todavía puede mejorar.

## S — Responsabilidad única

Cada componente tiene una responsabilidad reconocible. `IniciarSesionService` valida credenciales y coordina la autenticación; `RegistrarUsuarioService` registra cuentas; `JwtUtil` crea y valida tokens. La integración con la API de frases queda en `MotivationalApiAdapter`, mientras `ObtenerFraseDelDiaService` selecciona una frase utilizable y decide el respaldo.

Esto permite localizar cambios: una variación del proveedor HTTP de frases pertenece al adaptador; un cambio en cómo se elige la frase pertenece al caso de uso.

**Mejora pendiente:** algunos servicios, como `RecordatorioDiarioService`, dependen directamente de repositorios JPA e incluyen coordinación y formato del correo. Separar esos detalles detrás de puertos de aplicación haría más fácil probarlos y cambiar infraestructura.

## O — Abierto a extensión y cerrado a modificación

Los puertos de salida permiten añadir implementaciones sin modificar los casos de uso. `ProveedorFrasesMotivacionalesPort` es implementado por `MotivationalApiAdapter`; `UsuarioRepositoryPort` tiene su adaptador de persistencia. Un proveedor alternativo de frases podría implementar el mismo contrato y conectarse en infraestructura.

**Mejora pendiente:** revisar los componentes que todavía construyen o usan dependencias concretas en lugar de contratos, y definir cómo seleccionar implementaciones alternativas mediante configuración.

## L — Sustitución de Liskov

Los adaptadores que implementan un puerto deben respetar el contrato esperado por los casos de uso. Por ejemplo, el servicio de frases consume `ProveedorFrasesMotivacionalesPort` y recibe una lista de `FraseMotivacional`; el adaptador externo filtra registros sin frase y normaliza el autor antes de devolverlos. Los adaptadores de repositorio deben preservar las operaciones definidas por sus respectivos puertos.

**Mejora pendiente:** documentar en los puertos las garantías sobre valores nulos, errores y resultados vacíos, y cubrirlas para cada implementación intercambiable.

## I — Segregación de interfaces

Los puertos de entrada describen acciones concretas, como `IniciarSesionUseCase`, `RegistrarUsuarioUseCase` y `ObtenerFraseDelDiaUseCase`. Los puertos de salida separan necesidades como persistencia de usuarios, verificación de identidad de Google y envío de correo. Así cada caso de uso recibe contratos adecuados a su tarea.

**Mejora pendiente:** `UsuarioRepositoryPort` reúne varias consultas y operaciones de usuario; si sus consumidores crecen, se puede dividir por capacidades sin duplicar contratos innecesariamente.

## D — Inversión de dependencias

Los casos de uso se apoyan en puertos, no en detalles de base de datos o HTTP. `RegistrarUsuarioService` depende de `UsuarioRepositoryPort`, `PasswordEncoder` y `EnviarCorreoTransaccionalUseCase`. Infraestructura aporta adaptadores concretos, como `UsuarioRepositoryAdapter` y `ResendCorreoAdapter`.

La configuración de Spring conecta componentes en ejecución; los servicios pueden probarse con mocks de sus puertos sin levantar el contexto.

**Mejora pendiente:** extender este patrón a `RecordatorioDiarioService` y a otras clases de aplicación que hoy importan repositorios JPA directamente.
