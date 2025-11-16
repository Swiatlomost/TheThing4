import org.gradle.api.JavaVersion
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.thething.cos.lightledger"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = false
    }

    val generatedJniLibs = layout.buildDirectory.dir("generated/jniLibs")
    sourceSets {
        getByName("main") {
            jniLibs.srcDir(generatedJniLibs)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

val generatedJniDir = layout.buildDirectory.dir("generated/jniLibs")
val rustProjectDir = project.rootProject.file("native/light_ledger")
val enableCargoNdk = project.findProperty("enableCargoNdk") == "true"

fun cargoCommand(target: String): List<String> = listOf(
    "cargo",
    "ndk",
    "-t",
    target,
    "-o",
    generatedJniDir.get().asFile.absolutePath,
    "build",
    "--release"
)

val cargoBuildLightLedgerArm64 = tasks.register<Exec>("cargoBuildLightLedgerArm64") {
    group = "build"
    description = "Build Light Ledger native library via cargo-ndk (arm64-v8a)"
    onlyIf { enableCargoNdk }
    workingDir = rustProjectDir
    commandLine(cargoCommand("arm64-v8a"))
    doFirst { generatedJniDir.get().asFile.mkdirs() }
}

val cargoBuildLightLedgerX86 = tasks.register<Exec>("cargoBuildLightLedgerX86") {
    group = "build"
    description = "Build Light Ledger native library via cargo-ndk (x86_64)"
    onlyIf { enableCargoNdk }
    workingDir = rustProjectDir
    commandLine(cargoCommand("x86_64"))
    doFirst { generatedJniDir.get().asFile.mkdirs() }
    mustRunAfter(cargoBuildLightLedgerArm64)
}

tasks.named("preBuild").configure {
    if (enableCargoNdk) {
        dependsOn(cargoBuildLightLedgerArm64, cargoBuildLightLedgerX86)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")

    testImplementation("junit:junit:4.13.2")
}
