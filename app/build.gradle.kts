
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

fun getLocalProperty(key: String): String {
    return localProperties.getProperty(key, "")
}

android {
    namespace = "com.meupersonal"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.meupersonal"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${getLocalProperty("SUPABASE_URL")}\""
        )

        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            "\"${getLocalProperty("SUPABASE_PUBLISHABLE_KEY")}\""
        )

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${getLocalProperty("GEMINI_API_KEY")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.json:json:20240303")

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
