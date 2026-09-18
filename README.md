# Mis Gastos — App Android nativa

Proyecto Android (Kotlin + Jetpack Compose + Room) listo para abrir en Android Studio.

## Qué incluye
- **Dashboard**: total por mes, comparación vs. mes anterior, promedio mensual,
  categoría con mayor gasto, gráfico de barras por mes y gráfico circular con el
  detalle de la última categoría por mes.
- **Registrar**: eliges el mes desde un menú desplegable y editas cada categoría
  (Arriendo, Luz, Agua, Gas, Internet, TC Éxito, TC Rappi, Telefonía, Comida,
  Netflix, Univ./Smart, Adobe, Capcut, Google, Otro gasto). El total se actualiza
  en vivo y hay un botón "Guardar".
- **Tabla**: vista tipo hoja de cálculo con todos los meses y categorías, con scroll.
- Los datos quedan guardados **en el teléfono** con una base de datos Room (SQLite),
  no se pierden al cerrar la app.
- Ya viene precargada con tus datos reales de Enero a Septiembre (tomados de tu Excel),
  para que no partas de cero.

## Opción rápida: generar el .apk sin instalar nada (GitHub Actions)

Este proyecto ya incluye un workflow en `.github/workflows/build-apk.yml` que
compila el APK automáticamente en la nube. Pasos:

1. Crea un repositorio nuevo en GitHub (puede ser privado): https://github.com/new
2. Sube el contenido de esta carpeta `MisGastos` a ese repositorio. La forma más
   fácil sin usar la terminal: en la página del repo, "Add file" → "Upload files",
   arrastra todo el contenido de la carpeta y confirma el commit.
3. Ve a la pestaña **Actions** del repositorio. Debería aparecer y correr solo el
   workflow "Build APK" (tarda 3-5 minutos).
4. Cuando termine (ícono verde ✓), entra a esa ejecución y baja hasta
   **Artifacts** → descarga `mis-gastos-apk` (es un .zip que trae el .apk adentro).
5. Pasa el `.apk` a tu celular (por USB, WhatsApp, Drive, etc.) y ábrelo para
   instalarlo. Es posible que debas permitir "instalar apps de orígenes
   desconocidos" en Ajustes de Android.

No necesitas instalar Android Studio para este camino — todo se compila en los
servidores de GitHub.

## Alternativa: compilarlo tú mismo con Android Studio (genera el .apk)

1. Instala **Android Studio** (gratis): https://developer.android.com/studio
2. Abre Android Studio → "Open" → selecciona la carpeta `MisGastos` que descomprimiste.
3. Espera a que termine el "Gradle Sync" (la primera vez descarga dependencias,
   puede tardar unos minutos — necesitas internet).
4. Conecta tu celular Android por USB con "Depuración USB" activada
   (Ajustes → Acerca del teléfono → toca 7 veces "Número de compilación" para
   activar Opciones de desarrollador, luego actívala ahí), o usa un emulador.
5. Presiona el botón ▶ (Run) en Android Studio. Se instalará y abrirá la app
   directamente en tu celular.

### Si prefieres generar el .apk para instalarlo manualmente
En Android Studio: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`.
El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`; lo copias a tu
celular y lo instalas (puede que debas permitir "instalar apps de orígenes
desconocidos" en Ajustes).

## Estructura del proyecto
```
MisGastos/
├── app/
│   └── src/main/java/com/misgastos/app/
│       ├── MainActivity.kt        (navegación entre las 3 pantallas)
│       ├── data/                  (Room: entidad, DAO, base de datos, categorías, datos iniciales)
│       └── ui/                    (pantallas Dashboard, Registrar, Tabla, gráficos, tema, ViewModel)
├── build.gradle.kts
└── settings.gradle.kts
```

## Personalizar
- **Cambiar categorías**: edita `data/Categories.kt`.
- **Cambiar los datos precargados**: edita `data/SeedData.kt` (solo se usa la
  primera vez que se abre la app, si la base de datos está vacía).
- **Cambiar colores**: `ui/theme/Color.kt`.
