import org.gradle.api.JavaVersion
import java.util.Locale


plugins {    id("com.google.protobuf")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    fun Project.propOrEnv(key: String): String? {
        val prop = findProperty(key) as String?
        if (prop != null) return prop
        val envKey = key.replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase(Locale.ROOT)
        return System.getenv(envKey)
    }

    val uploadStoreFile = project.propOrEnv("poiUploadStoreFile")
    val uploadStorePassword = project.propOrEnv("poiUploadStorePassword")
    val uploadKeyAlias = project.propOrEnv("poiUploadKeyAlias")
    val uploadKeyPassword = project.propOrEnv("poiUploadKeyPassword")
    val requiresUploadSigning = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

    signingConfigs {
        val debug = getByName("debug")
        create("upload") {
            if (!requiresUploadSigning && uploadStoreFile == null) {
                storeFile = debug.storeFile
                storePassword = debug.storePassword
                keyAlias = debug.keyAlias
                keyPassword = debug.keyPassword
            } else {
                val storeFilePath = uploadStoreFile
                    ?: throw GradleException("Missing release signing: set poiUploadStoreFile or POI_UPLOAD_STORE_FILE")
                storeFile = rootProject.file(storeFilePath)
                storePassword = uploadStorePassword
                    ?: throw GradleException("Missing release signing: set poiUploadStorePassword or POI_UPLOAD_STORE_PASSWORD")
                keyAlias = uploadKeyAlias
                    ?: throw GradleException("Missing release signing: set poiUploadKeyAlias or POI_UPLOAD_KEY_ALIAS")
                keyPassword = uploadKeyPassword
                    ?: throw GradleException("Missing release signing: set poiUploadKeyPassword or POI_UPLOAD_KEY_PASSWORD")
            }
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.thething.cos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thething.cos"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("upload")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    // Protobuf sources are placed under src/main/proto

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":feature:cos-lifecycle"))
    implementation(project(":feature:cos-overlay"))
    implementation(project(":feature:morphogenesis"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:sensor-harness"))
    implementation(project(":core:lightledger"))

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    implementation("com.google.android.play:integrity:1.5.0")
    implementation("com.google.android.gms:play-services-tasks:18.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // For javax.annotation.Generated used in generated gRPC stubs
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}


protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.3" }
    plugins {
        create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.62.2" }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins { create("grpc") { option("lite") } }
            task.builtins { create("java") { option("lite") } }
        }
    }
}

dependencies {
    implementation("io.grpc:grpc-okhttp:1.62.2")
    implementation("io.grpc:grpc-protobuf-lite:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("com.google.protobuf:protobuf-javalite:3.25.3")
}


