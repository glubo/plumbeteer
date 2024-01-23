import com.lehaine.littlekt.gradle.texturepacker.littleKt
import com.lehaine.littlekt.gradle.texturepacker.packing
import com.lehaine.littlekt.gradle.texturepacker.texturePacker

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.bundles.plugins)
    }
//    dependencies {
//        classpath("com.lehaine.littlekt.gradle:texturepacker:0.6.0")
//    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    group = "com.game.template"
    version = "1.0"
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
        yarnLockMismatchReport = org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport.WARNING
    }
}

plugins {
    id("com.lehaine.littlekt.gradle.texturepacker") version "0.1.0"
}
littleKt {
    texturePacker {
        inputDir = "art/export_tiles/"
        outputDir = "game/src/commonMain/resources/"
        outputName = "tiles.atlas"

        packing {
            allowRotation = true
        }
    }
}
