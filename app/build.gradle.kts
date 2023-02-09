import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    id("com.android.application")
    id("checkstyle")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android")
    kotlin("kapt")
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.vanniktech.android.junit.jacoco") version "0.16.0"
    id("dagger.hilt.android.plugin")
    id("com.google.android.gms.oss-licenses-plugin")
}

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.tasks")
            includeModule("com.github.bitfireAT", "cert4android")
            includeModule("com.github.bitfireAT", "dav4jvm")
            includeModule("com.github.tasks.opentasks", "opentasks-provider")
            includeModule("com.github.QuadFlask", "colorpicker")
            includeModule("com.github.twofortyfouram", "android-plugin-api-for-locale")
            includeModule("com.github.franmontiel", "PersistentCookieJar")
        }
    }
}

android {
    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    lint {
        lintConfig = file("lint.xml")
        textOutput = File("stdout")
        textReport = true
    }

    compileSdk = Versions.compileSdk

    defaultConfig {
        testApplicationId = "org.tasks.test"
        applicationId = "org.tasks"
        versionCode = 130200
        versionName = "13.2"
        targetSdk = Versions.targetSdk
        minSdk = Versions.minSdk
        testInstrumentationRunner = "org.tasks.TestRunner"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
                arg("room.incremental", "true")
            }
        }
    }

    signingConfigs {
        create("release") {
            val tasksKeyAlias: String? by project
            val tasksStoreFile: String? by project
            val tasksStorePassword: String? by project
            val tasksKeyPassword: String? by project

            keyAlias = tasksKeyAlias
            storeFile = file(tasksStoreFile ?: "none")
            storePassword = tasksStorePassword
            keyPassword = tasksKeyPassword
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose_compiler
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    flavorDimensions += listOf("store")

    @Suppress("LocalVariableName")
    buildTypes {
        getByName("debug") {
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            val tasks_mapbox_key_debug: String? by project
            val tasks_google_key_debug: String? by project
            val tasks_caldav_url: String? by project
            resValue("string", "mapbox_key", tasks_mapbox_key_debug ?: "")
            resValue("string", "google_key", tasks_google_key_debug ?: "")
            resValue("string", "tasks_caldav_url", tasks_caldav_url ?: "https://caldav.tasks.org")
            resValue("string", "tasks_nominatim_url", tasks_caldav_url ?: "https://nominatim.tasks.org")
            resValue("string", "tasks_places_url", tasks_caldav_url ?: "https://places.tasks.org")
            isTestCoverageEnabled = project.hasProperty("coverage")
        }
        getByName("release") {
            val tasks_mapbox_key: String? by project
            val tasks_google_key: String? by project
            resValue("string", "mapbox_key", tasks_mapbox_key ?: "")
            resValue("string", "google_key", tasks_google_key ?: "")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    productFlavors {
        create("generic") {
            dimension = "store"
        }
        create("googleplay") {
            dimension = "store"
        }
    }
    packagingOptions {
        resources {
            excludes += setOf("META-INF/*.kotlin_module")
        }
    }

    namespace = "org.tasks"
}

configure<CheckstyleExtension> {
    configFile = project.file("google_checks.xml")
    toolVersion = "8.16"
}

configurations.all {
    exclude(group = "org.apache.httpcomponents")
    exclude(group = "org.checkerframework")
    exclude(group = "com.google.code.findbugs")
    exclude(group = "com.google.errorprone")
    exclude(group = "com.google.j2objc")
    exclude(group = "com.google.http-client", module = "google-http-client-apache-v2")
    exclude(group = "com.google.http-client", module = "google-http-client-jackson2")
}

val genericImplementation by configurations
val googleplayImplementation by configurations

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.0")
    implementation("com.github.bitfireAT:dav4jvm:2.2") {
        exclude(group = "junit")
    }
    implementation("com.github.tasks:ical4android:27dc5bf") {
        exclude(group = "commons-logging")
        exclude(group = "org.json", module = "json")
        exclude(group = "org.codehaus.groovy", module = "groovy")
        exclude(group = "org.codehaus.groovy", module = "groovy-dateutil")
    }
    implementation("com.github.bitfireAT:cert4android:7814052")
    implementation("com.github.tasks.opentasks:opentasks-provider:562fec5") {
        exclude("com.github.tasks.opentasks", "opentasks-contract")
    }
    implementation("org.dmfs:rfc5545-datetime:0.2.4")
    implementation("org.dmfs:lib-recur:0.11.4")
    implementation("org.dmfs:jems:1.33")

    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-compiler:${Versions.hilt}")
    kapt("androidx.hilt:hilt-compiler:${Versions.hilt_androidx}")
    implementation("androidx.hilt:hilt-work:${Versions.hilt_androidx}")

    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.room:room-ktx:${Versions.room}")
    kapt("androidx.room:room-compiler:${Versions.room}")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.paging:paging-runtime:3.1.1")
    implementation("io.noties.markwon:core:${Versions.markwon}")
    implementation("io.noties.markwon:editor:${Versions.markwon}")
    implementation("io.noties.markwon:ext-tasklist:${Versions.markwon}")
    implementation("io.noties.markwon:ext-strikethrough:${Versions.markwon}")
    implementation("io.noties.markwon:ext-tables:${Versions.markwon}")
    implementation("io.noties.markwon:linkify:${Versions.markwon}")

    debugImplementation("com.facebook.flipper:flipper:${Versions.flipper}")
    debugImplementation("com.facebook.flipper:flipper-network-plugin:${Versions.flipper}")
    debugImplementation("com.facebook.soloader:soloader:0.10.5")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation("com.squareup.okhttp3:okhttp:${Versions.okhttp}")
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.preference:preference:1.2.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.google.android.apps.dashclock:dashclock-api:2.0.0")
    implementation("com.github.twofortyfouram:android-plugin-api-for-locale:1.0.2") {
        isTransitive = false
    }
    implementation("com.rubiconproject.oss:jchronic:0.2.6") {
        isTransitive = false
    }
    implementation("me.leolin:ShortcutBadger:1.1.22@aar")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20210709-1.32.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20210725-1.32.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:0.26.0")
    implementation("androidx.work:work-runtime:${Versions.work}")
    implementation("androidx.work:work-runtime-ktx:${Versions.work}")
    implementation("com.etebase:client:2.3.2")
    implementation("com.github.QuadFlask:colorpicker:0.0.15")
    implementation("net.openid:appauth:0.11.1")
    implementation("org.osmdroid:osmdroid-android:6.1.11@aar")
    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:converter-moshi:${Versions.retrofit}")
    implementation("androidx.recyclerview:recyclerview:1.3.0-rc01")

    implementation(platform("androidx.compose:compose-bom:${Versions.compose_bom}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("com.google.android.material:compose-theme-adapter:${Versions.compose_theme_adapter}")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.compose.ui:ui-viewbinding")
    implementation("io.coil-kt:coil-compose:${Versions.coil}")
    implementation("io.coil-kt:coil-video:${Versions.coil}")
    implementation("io.coil-kt:coil-svg:${Versions.coil}")
    implementation("io.coil-kt:coil-gif:${Versions.coil}")
    releaseCompileOnly("androidx.compose.ui:ui-tooling")

    implementation("com.google.accompanist:accompanist-flowlayout:${Versions.accompanist}")
    implementation("com.google.accompanist:accompanist-permissions:${Versions.accompanist}")

    googleplayImplementation("com.google.firebase:firebase-crashlytics:${Versions.crashlytics}")
    googleplayImplementation("com.google.firebase:firebase-analytics:${Versions.analytics}") {
        exclude("com.google.android.gms", "play-services-ads-identifier")
    }
    googleplayImplementation("com.google.firebase:firebase-config-ktx:${Versions.remote_config}")
    googleplayImplementation("com.google.android.gms:play-services-location:19.0.1")
    googleplayImplementation("com.google.android.gms:play-services-maps:18.1.0")
    googleplayImplementation("com.android.billingclient:billing-ktx:4.0.0")
    googleplayImplementation("com.google.android.play:core:1.10.3")
    googleplayImplementation("com.google.android.play:core-ktx:1.8.1")
    googleplayImplementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

    androidTestImplementation("com.google.dagger:hilt-android-testing:${Versions.hilt}")
    kaptAndroidTest("com.google.dagger:hilt-compiler:${Versions.hilt}")
    kaptAndroidTest("androidx.hilt:hilt-compiler:${Versions.hilt_androidx}")
    androidTestImplementation("org.mockito:mockito-android:${Versions.mockito}")
    androidTestImplementation("com.natpryce:make-it-easy:${Versions.make_it_easy}")
    androidTestImplementation("androidx.test:runner:${Versions.androidx_test}")
    androidTestImplementation("androidx.test:rules:${Versions.androidx_test}")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:${Versions.okhttp}")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("com.natpryce:make-it-easy:${Versions.make_it_easy}")
    testImplementation("androidx.test:core:${Versions.androidx_test}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.ogce:xpp3:1.1.6")
}
