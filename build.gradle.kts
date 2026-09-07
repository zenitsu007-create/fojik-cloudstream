buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.kotlin.android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        val cloudstream by configurations
        cloudstream("com.lagradost:cloudstream3:pre-release")
        implementation("org.jsoup:jsoup:1.18.3")
    }

    android {
        compileSdk = 35
        namespace = "com.fojik.cloudstream"
        defaultConfig {
            minSdk = 21
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    cloudstream {
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "YOUR_GITHUB_USERNAME/FojikCloudStream")
    }
}
