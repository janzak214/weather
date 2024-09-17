import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.parcelize)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting
        val androidInstrumentedTest by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.sqldelight.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compass.geolocation.mobile)
            implementation(libs.kotlinLogging.android)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.logback.android)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(compose.preview)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.coroutinesextensions)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.composeIcons.weatherIcons)
            implementation(libs.compass.geolocation)
            implementation(libs.kotlinx.datetime)
            implementation(libs.materialKolor)
            implementation(libs.store5)
            implementation(libs.sandwich)
            implementation(libs.sandwich.ktor)
            compileOnly(libs.kotlinLogging)
            implementation(libs.slf4j.api)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.sqldelight.sqlite)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.slf4j.simple)
            implementation(libs.kotlinLogging.jvm)
            implementation(libs.appdirs)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))

            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.junit)
            implementation(libs.kotlin.test.annotations)
            implementation(libs.kotlinx.coroutines.test)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }
}

android {
    namespace = "pl.janzak.weather"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "pl.janzak.weather"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    applicationVariants.configureEach {
        val variant = name
        val version = versionName

        outputs.configureEach {
            if (this is ApkVariantOutputImpl) {
                outputFileName = "weather-${variant}-$version.apk"
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Weather"
            packageVersion = "1.0.0"
            vendor = "Jan Zakrzewski"

            windows {
                installationPath = "pl.janzak.weather"
                includeAllModules = true
                iconFile.set(projectDir.parentFile.resolve("images/icon-rounded.ico"))
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "17764e10-9057-426b-989b-4c198312cf02"
                menu = true
                shortcut = true
            }

            buildTypes {
                release {
                    proguard {
                        isEnabled = false
                    }
                }
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "resources"
    generateResClass = always
}

sqldelight {
    databases {
        create("Database") {
            packageName = "pl.janzak.weather.database"
        }
    }
}
