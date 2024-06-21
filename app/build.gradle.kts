plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeCompiler)

}

android {
    namespace = "com.example.newcollage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.newcollage"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Do NOT compress tflite model files (need to call out to developers!)
    androidResources {
        noCompress += "tflite"
    }
}



dependencies {
    // android常用的一些官方库
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)


    //room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    //compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.size)
    implementation(libs.androidx.activity.compose)

    //glide and coil，图片加载
    implementation(libs.glide)
    implementation(libs.glide.compose)
    implementation(libs.coil.compose)

    //androidx shape
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.graphics.path)

    //权限
    implementation(libs.permissionx)

    //mlkit的主题分割
    implementation(libs.segmentation.selfie)
    implementation(libs.play.services)
    implementation(libs.subject.segmentation)


    //test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    //debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

