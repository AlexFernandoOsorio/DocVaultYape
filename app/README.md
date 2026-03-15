# DocVault 🔐

Aplicación Android de gestión de documentos confidenciales con protección biométrica, cifrado AES-256 y marca de agua con geolocalización.

---

## 📐 Arquitectura: MVI + Clean Architecture

```
com.example.docvaultyape/
├── core/                    # Utilidades transversales (no dependen del framework)
│   ├── biometric/           # BiometricAuthManager
│   ├── crypto/              # EncryptionManager (AES-256-GCM)
│   └── location/            # LocationManager (FusedLocation + Geocoder)
├── data/                    # Capa de datos
│   ├── local/               # Room (entities, DAOs, Database)
│   └── repository/          # Implementaciones de repositorios
├── di/                      # Módulos Hilt
├── domain/                  # Capa de dominio (pura Kotlin, sin Android)
│   ├── model/               # Modelos de negocio
│   ├── repository/          # Interfaces de repositorio
│   └── usecase/             # Casos de uso
└── presentation/            # Capa de presentación (Jetpack Compose + MVI)
    ├── navigation/          # NavGraph con Navigation Compose
    ├── screens/
    │   ├── home/            # HomeScreen + HomeViewModel + Interactors (MVI)
    │   └── detail/          # DocumentDetailScreen + DetailViewModel + Interactors (MVI)
    └── theme/               # Material3 Dark Theme
```

### ¿Por qué MVI?

El patrón **Model-View-Intent** fue elegido por:

- **Unidireccionalidad**: El flujo de datos es predecible (Intent → ViewModel → State → UI)
- **Estado inmutable**: `data class` como estado garantiza que la UI siempre refleja un estado consistente
- **Testabilidad**: Los ViewModels pueden probarse sin UI. Los Intents son los inputs, los States los outputs
- **Debugging**: Es sencillo reproducir cualquier estado de la app en un test
- **Compatibilidad con Compose**: Compose es reactivo por naturaleza; MVI encaja perfectamente con `collectAsState()`

### Contrato MVI por pantalla

Cada pantalla tiene tres artefactos:
- **`XInteractors.kt`**: Define `XState`, `XIntent`, `XEvent`
- **`XViewModel.kt`**: Procesa `Intent`s → actualiza `State` → emite `Event`s de un solo disparo
- **`XScreen.kt`**: Observa `State`, lanza `Intent`s, reacciona a `Event`s

---

## 🔒 Seguridad

### Cifrado AES-256-GCM

- Se utiliza el **Android Keystore System** para almacenar la clave criptográfica de forma segura dentro del hardware del dispositivo (TEE/StrongBox cuando disponible)
- **Algoritmo**: AES/GCM/NoPadding con clave de 256 bits
- **IV**: 12 bytes aleatorios generados por el sistema, prepended al archivo cifrado
- **Tag de autenticación**: 128 bits (detecta modificaciones del archivo)
- Formato en disco: `[IV 12 bytes][Datos cifrados con tag GCM]`
- Los archivos originales nunca se guardan en disco; solo existe la versión cifrada en `filesDir/secure_docs/`
- Los archivos temporales de visualización se guardan en `cacheDir/temp_view/` y se limpian al cerrar

### Biométrica

- Usa `androidx.biometric.BiometricPrompt` con `BIOMETRIC_STRONG`
- Fallback a `DEVICE_CREDENTIAL` (PIN/patrón) si no hay biométrico configurado
- La autenticación se requiere **cada vez** que se abre la pantalla de detalle
- La eliminación también requiere confirmación explícita post-autenticación
- Requiere `AppCompatActivity` como base de `MainActivity` para que `BiometricPrompt` pueda acceder al `FragmentManager`

### Prevención de capturas de pantalla

- `WindowManager.LayoutParams.FLAG_SECURE` se activa al entrar a `DocumentDetailScreen` y se limpia al salir con `DisposableEffect`
- Esta flag previene screenshots, screen recording y aparición en el reciente de aplicaciones

### Marca de agua geolocalizada

- Se dibuja directamente sobre el canvas de Compose con `drawWithContent`
- Usa alpha ~22% para ser semi-transparente
- Contiene la dirección de calle obtenida via `Geocoder` al momento de importar el documento
- La dirección incluye: calle, número, ciudad, estado, país

---

## 🗂️ Control de versiones — Git

Se utilizó **Git** como sistema de control de versiones siguiendo una estrategia de ramas ordenada y commits semánticos.

### Estrategia de ramas

| Rama | Propósito |
|---|---|
| `main` | Código en producción, siempre estable |
| `develop` | Integración de features en desarrollo |
|

## 🔍 Análisis estático — Detekt

Se configuró **Detekt** para análisis estático del código Kotlin, detectando problemas de complejidad, malas prácticas y bugs potenciales que el compilador no reporta.

### Configuración

```kotlin
// app/build.gradle.kts
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.5")
}
```

### Ejecutar Detekt

```bash
# Analizar
./gradlew detekt

# Ver reporte
open app/build/reports/detekt/detekt.html
```

## 📚 Librerías utilizadas

| Librería | Versión | Motivo |
|---|---|---|
| Jetpack Compose BOM | 2024.09.03 | UI declarativa |
| Material3 | via BOM | Design system moderno |
| Hilt | 2.52 | Inyección de dependencias |
| Room | 2.6.1 | Persistencia local con Flow |
| Navigation Compose | 2.8.2 | Navegación type-safe |
| Biometric | 1.2.0-alpha05 | Autenticación biométrica |
| Coil Compose | 2.7.0 | Carga de imágenes cifradas desde disco |
| Accompanist Permissions | 0.36.0 | Permisos en tiempo de ejecución con Compose |
| Play Services Location | 21.3.0 | FusedLocationProviderClient |
| KSP | 2.0.21-1.0.25 | Procesamiento de anotaciones Room/Hilt |
| Detekt | 1.23.7 | Análisis estático de código Kotlin |
| MockK | 1.13.12 | Mocking en tests |

---

## 🧪 Tests

### Unitarios (`/test`)

- **`GetDocumentsUseCaseTest`**: Valida filtrado por tipo (PDF/IMAGE/todos)
- **`AddDocumentUseCaseTest`**: Valida agregar imagen/PDF, fallo de cifrado, ubicación nula
- **`DeleteDocumentUseCaseTest`**: Valida eliminación exitosa y manejo de errores
- **`GetDecryptedFileUseCaseTest`**: Valida descifrado, registro de acceso VIEW y fallo sin log
- **`GetAccessLogsUseCaseTest`**: Valida logs por documento, lista vacía y diferentes acciones
- **`DocumentRepositoryImplTest`**: Valida mapeo de entities, CRUD y cifrado/descifrado
- **`EncryptionManagerTest`**: Valida lógica de directorios y nomenclatura de archivos
- **`HomeViewModelTest`**: Valida estados del ViewModel (carga, filtrado, toggle FAB)
- **`DocumentDetailViewModelTest`**: Valida autenticación, descifrado, eliminación y efectos de navegación

### Automatizados de UI (`/androidTest`)

- **`HomeScreenTest`**: Valida lista de documentos, filtros, FAB y navegación a detalle
- **`DocumentDetailScreenTest`**: Valida pantalla biométrica, botón volver y nombre en top bar
- **`NavigationTest`**: Valida flujo completo Home → Detalle → Home

### Ejecutar tests

```bash
# Pruebas unitarias
./gradlew test

# Pruebas de UI (requiere emulador o dispositivo conectado)
./gradlew connectedAndroidTest

# Lint + Detekt + Tests
./gradlew lint detekt test

```

---

## 🚀 Cómo compilar

```bash
# Debug
./gradlew assembleDebug

# Tests
./gradlew test
```

### Requisitos
- Android Studio Hedgehog o superior
- SDK mínimo: API 26 (Android 8.0)
- JDK 17

---

## ✅ Checklist de requerimientos

| Requerimiento | Estado |
|---|---|
| Lista de documentos | ✅ |
| Filtro por tipo (PDF/Imagen) | ✅ |
| Agregar desde galería | ✅ |
| Agregar desde cámara | ✅ |
| Cifrado AES-256 (Android Keystore) | ✅ |
| Autenticación biométrica para visualización | ✅ |
| Prevención de screenshots (FLAG_SECURE) | ✅ |
| Pinch-to-zoom en imágenes | ✅ |
| Marca de agua con geolocalización (calle) | ✅ |
| Registro de accesos con fecha y hora | ✅ |
| Eliminación tras autenticación biométrica | ✅ |
| Kotlin + Jetpack Compose | ✅ |
| MVI Pattern | ✅ |
| Clean Architecture | ✅ |
| Hilt DI | ✅ |
| Control de versiones con Git | ✅ |
| Análisis estático con Detekt | ✅ |
| Pruebas unitarias | ✅ |
| Pruebas automatizadas de UI | ✅ |
| README con decisiones | ✅ |

---

## 💡 Decisiones de diseño

### ¿Por qué `cacheDir` para archivos temporales?

Los archivos descifrados para visualización se colocan en `cacheDir` (no en `filesDir`) para que el sistema operativo pueda limpiarlos automáticamente bajo presión de almacenamiento, reduciendo la ventana de exposición del contenido en claro.

### ¿Por qué Room y no DataStore?

Los documentos son entidades relacionales (documentos ↔ access_logs con FK y CASCADE). Room es la herramienta correcta para datos relacionales con soporte nativo de `Flow`.

### ¿Por qué `AppCompatActivity` y no `ComponentActivity`?

`BiometricPrompt` requiere `FragmentActivity` internamente para mostrar el diálogo biométrico via `FragmentManager`. `AppCompatActivity` extiende `FragmentActivity` y es 100% compatible con Compose, Hilt y `enableEdgeToEdge()`.

### ¿Por qué KSP y no KAPT?

KSP (Kotlin Symbol Processing) es hasta 2x más rápido que KAPT para el procesamiento de anotaciones de Room y Hilt, ya que trabaja directamente con el árbol sintáctico de Kotlin en lugar de generar Java stubs intermedios.

### ¿Por qué Detekt además de Android Lint?

Android Lint conoce el ecosistema Android pero no analiza la calidad estructural del código Kotlin. Detekt complementa detectando métodos demasiado complejos, parámetros excesivos, variables no usadas y otros problemas que afectan la mantenibilidad del código a largo plazo.

---