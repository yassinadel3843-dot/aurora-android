import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "com.aurora.workspace"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.aurora.workspace"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    // Release signing. A published build must keep the same key forever:
    // Android refuses to install an update signed with a different one, so the
    // in-app updater depends on this staying stable.
    //
    // Sources, in order: `keystore.properties` at the repo root (local, ignored
    // by git) and then environment variables (what GitHub Actions provides).
    val secretProps = Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
    fun secret(property: String, env: String): String? =
        secretProps.getProperty(property)?.takeIf { it.isNotBlank() }
            ?: System.getenv(env)?.takeIf { it.isNotBlank() }

    signingConfigs {
        create("release") {
            val storePath = secret("storeFile", "AURORA_STORE_FILE")
            val storePass = secret("storePassword", "AURORA_STORE_PASSWORD")
            val alias = secret("keyAlias", "AURORA_KEY_ALIAS")
            val keyPass = secret("keyPassword", "AURORA_KEY_PASSWORD")

            if (storePath != null && storePass != null && alias != null && keyPass != null) {
                storeFile = file(storePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            } else {
                // Fallback so a fresh clone can still build an installable APK.
                // Never use this key for published releases.
                val debugKeystore =
                    file("${System.getProperty("user.home")}/.android/debug.keystore")
                if (debugKeystore.exists()) {
                    storeFile = debugKeystore
                    storePassword = "android"
                    keyAlias = "androiddebugkey"
                    keyPassword = "android"
                }
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources.excludes += setOf("META-INF/*.kotlin_module", "META-INF/DEPENDENCIES")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    testImplementation("junit:junit:4.13.2")
    // Real org.json for JVM tests: the update parser reads the GitHub payload
    // with it, and the mockable android.jar only contains throwing stubs.
    testImplementation("org.json:json:20240303")
}
