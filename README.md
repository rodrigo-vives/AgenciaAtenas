# Agencia Atenas

Aplicación Android académica para gestionar oportunidades y activaciones corporativas con información ficticia. Almacena los datos localmente mediante Room/SQLite; no requiere un servidor.

## Integrantes

- Omar Martínez
- Rodrigo Vivez

Sección y enlace del repositorio: pendientes de informar por el equipo.

## Abrir y ejecutar

1. Descomprimir el proyecto y abrir su carpeta raíz en Android Studio, donde están settings.gradle.kts y gradlew.bat.
2. Usar el JDK incluido con Android Studio y disponer del SDK que pide app/build.gradle.kts: compileSdk/targetSdk 37, minSdk 24.
3. Sincronizar dependencias; la primera vez requiere conexión a internet.
4. Seleccionar un emulador o celular con Android 7.0/API 24 o posterior y ejecutar el módulo app.
5. Entrar con una cuenta ficticia de la tabla siguiente. Las cuentas que falten se crean al autenticar.

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| admin | 1234 | Supervisor comercial |
| ejecutivo1 | 1234 | Ejecutivo comercial |
| ejecutivo2 | 1234 | Ejecutivo comercial |
| ejecutivo3 | 1234 | Ejecutivo comercial |

Las oportunidades pertenecen al usuario creador hasta que el supervisor las reasigna. Un ejecutivo solo ve las suyas. El supervisor puede verlas todas y confirmar/descartar según las transiciones permitidas.

## Funcionalidades

- Login local y cierre de sesión.
- Inicio por rol, próximos eventos y avisos internos de acciones pendientes/atrasadas, oportunidades sin contacto y propuestas sin respuesta.
- Listado con búsqueda por nombre y filtros combinables: todos los estados, prioridad, tipo, ubicación (comuna/región/recinto), organización, responsable para supervisor y rango inclusivo de fecha de inicio.
- Crear/editar oportunidad, validaciones y detalle agrupado por secciones.
- Registro/consulta de organizaciones y contactos ficticios con relaciones por ID.
- Seguimientos, programación de próxima acción, resolución explícita e historial de cambios/asignaciones.
- Panel de supervisor con asignación/reasignación.
- Cámara externa, fotos persistentes por oportunidad, vista previa y ampliación.
- Apertura del calendario nativo con datos del evento para guardar o cancelar allí.
- Tres interacciones animadas de fotos con AnimatedVisibility y Crossfade.

Los nueve destinos originales eran: Login, Inicio, Lista, Crear/Editar, Detalle, Organizaciones, Contactos, Seguimiento y Supervisor. Crear/Editar reutiliza un formulario; Organizaciones y Contactos reutilizan el componente de catálogo con rutas distintas. Ahora se agregan Mi perfil, Historial de contacto, Historial de organización y Resultado de activación: trece destinos lógicos en total.

## Arquitectura y datos

UI (Compose) -> ViewModel (StateFlow) -> Repository -> DAO -> Room.

Las entidades son Usuario, Organización, Contacto, Oportunidad, OportunidadContacto, Seguimiento y ResultadoActivacion. Se usan claves primarias y foráneas; los nombres de catálogo se consultan por JOIN. OportunidadContacto selecciona un contacto principal por oportunidad. Una próxima acción resuelta apunta al seguimiento que registró su resultado.

AppDatabase crea agencia_atenas.db, versión 5, en el almacenamiento privado del dispositivo. No contiene migraciones históricas ni borrado automático de datos incompatibles. Una instalación nueva crea directamente el esquema actual; una instalación con esquema antiguo necesita reiniciar sus datos de prueba. Cerrar la app no borra registros. El ZIP del código no incluye los registros del emulador.

Las fotografías están en filesDir/evidencias/ID/ y no dentro de SQLite. FileProvider comparte solo esa carpeta mediante permisos temporales. No se cambia el esquema de Room para añadir fotos o filtros.

## Recursos nativos

La cámara requiere una aplicación de cámara disponible. La captura se delega mediante TakePicture y FileProvider, sin acceso directo al hardware desde Agencia Atenas.

Calendar requiere una aplicación que atienda ACTION_INSERT y un calendario/cuenta configurado cuando esa aplicación lo exija. Revisa y pulsa Guardar allí. Abrir el editor no confirma el guardado. No se solicitan READ_CALENDAR/WRITE_CALENDAR. Los eventos sin horario se exportan como día completo.

No se envían notificaciones del sistema, correos ni mensajes. Los recordatorios son internos. Repetir la exportación a Calendar puede producir duplicados si se guarda varias veces.

## Comprobaciones

Última revisión de legibilidad: 18/10/2026. Consultar REVISION_GENERAL_EQUIPO.md para cambios, comparación con PPT y recorrido de estudio.

Comandos desde la raíz, con JAVA_HOME/JDK configurado:

```text
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:lintDebug
```

En Windows usar gradlew.bat. connectedDebugAndroidTest requiere un dispositivo conectado. Lint y la primera compilación pueden descargar herramientas. El informe de pruebas unitarias queda en app/build/reports/tests/testDebugUnitTest/index.html.

Consultar CUMPLIMIENTO_REQUISITOS.md para el estado vigente: 41 pruebas unitarias y 11 instrumentales aprobadas. REVISION_Y_ENTREGA.md conserva la pauta manual anterior. No confundir compilar pruebas instrumentales con ejecutarlas ni preparación de entrega con envío a AVA.
