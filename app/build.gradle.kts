plugins {
    alias(libs.plugins.android.application)
}

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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    implementation("androidx.room:room-runtime:2.8.3")
    annotationProcessor("androidx.room:room-compiler:2.8.3")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}