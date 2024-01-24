plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}


android {
    namespace = "org.samo_lego.locallm"
    compileSdk = 34

    val jllamaLib = file("libs/java-llama.cpp")

    // Execute "mvn compile" if folder target/ doesn't exist at ./libs/java-llama.cpp/
    if (!file("$jllamaLib/target").exists()) {
        exec {
            commandLine = listOf("mvn", "compile")
            workingDir = file("libs/java-llama.cpp/")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }


    defaultConfig {
        applicationId = "org.samo_lego.locallm"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


        //val libs = file("libs/").absolutePath

        externalNativeBuild {
            cmake {
                cppFlags += ""
                //arguments += "-DCMAKE_VERBOSE_MAKEFILE=ON"
                //arguments += "-DLLAMA_CLBLAST=ON"  // Enable CLBlast
                //arguments += "-DCLBlast_DIR=$libs/clblast"  // Enable CLBlast
                //arguments += "-DLLAMA_BLAS=ON"  // Enable BLAS
                //arguments += "-DLLAMA_BLAS_VENDOR=\"OpenBLAS\""  // Use OpenBLAS
                //arguments += "-DBLAS_INCLUDE_DIRS=$libs/openblas"
                //arguments += "-DBLAS_LIBRARIES=$libs/openblas"
                //arguments += "-DCMAKE_C_FLAGS=-fopenmp"
                //arguments += "--debug-find"
                //arguments += "--trace-expand"
                //arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isJniDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    externalNativeBuild {
        cmake {
            path = file("$jllamaLib/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets {
        named("main") {
            // Add source directory for java-llama.cpp
            java.srcDir("$jllamaLib/src/main/java")
        }
    }

    // Apply patch files on build
    /*task("applyPatches") {
        doFirst {
            val patchDir = file("patch/")
            if (patchDir.exists()) {
                patchDir.listFiles()?.forEach { patchFile ->
                    if (patchFile.extension == "patch") {
                        println("Applying patch ${patchFile.name}...")
                        println("Executing: patch --directory=$jllamaLib -p1 < ${patchFile.absolutePath}")
                        val processBuilder =
                            ProcessBuilder("patch", "--directory=$jllamaLib", "-p1")
                        processBuilder.redirectInput(patchFile.absoluteFile)
                        val process = processBuilder.start()
                        process.waitFor()
                    }
                }
            }
        }
    }

    tasks.build {
        dependsOn("applyPatches")
    }*/
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //implementation("de.kherud:llama:2.3.2")
}
