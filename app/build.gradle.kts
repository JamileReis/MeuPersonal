import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val propriedadesLocais = Properties().apply {
    val arquivo = rootProject.file("local.properties")

    if (arquivo.exists()) {
        arquivo.inputStream().use { load(it) }
    }
}

fun String.toBuildConfigValue(): String =
    "\"" + replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r") + "\""

val supabaseUrl = propriedadesLocais
    .getProperty("SUPABASE_URL", "").trim()

val supabaseChave = propriedadesLocais
    .getProperty("SUPABASE_PUBLISHABLE_KEY", "").trim()

val geminiTrainingApiKey = propriedadesLocais
    .getProperty("GEMINI_TRAINING_API_KEY", "").trim()

val geminiImageApiKey = propriedadesLocais
    .getProperty("GEMINI_IMAGE_API_KEY", "").trim()

android {
    namespace = "com.meupersonal"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.meupersonal"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            supabaseUrl.toBuildConfigValue()
        )

        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            supabaseChave.toBuildConfigValue()
        )

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            geminiTrainingApiKey.toBuildConfigValue()
        )

        buildConfigField(
            "String",
            "GEMINI_TRAINING_API_KEY",
            geminiTrainingApiKey.toBuildConfigValue()
        )

        buildConfigField(
            "String",
            "GEMINI_IMAGE_API_KEY",
            geminiImageApiKey.toBuildConfigValue()
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }

        release {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    implementation("androidx.room:room-runtime:2.8.3")
    annotationProcessor("androidx.room:room-compiler:2.8.3")

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.1.5"
    )

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.json:json:20240303")

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
