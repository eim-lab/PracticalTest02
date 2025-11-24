plugins {
    id("com.android.application")
}

android {
    namespace = "ro.pub.cs.systems.eim.practicaltest02"
    // Update SDK versions to a modern API level for AndroidX compatibility
    compileSdk = 35

    defaultConfig {
        applicationId = "ro.pub.cs.systems.eim.practicaltest02"
        minSdk = 24
        // Target SDK should match compile SDK
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            // This is often not needed with modern AGP versions but keeping it just in case
            pickFirsts += "META-INF/okio.kotlin_module"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor")
}
