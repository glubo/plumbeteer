import korlibs.korge.gradle.korge


plugins {
    alias(libs.plugins.korge)
}

korge {
    id = "cz.glubo.plumbeteer"
    name = "plumbeteer"
    androidTargetSdk = 34
    version = "0.0.2"
    versionCode = 2

// To enable all targets at once

    // targetAll()

// To enable targets based on properties/environment variables
    // targetDefault()

    // To selectively enable targets
    fun isEnabled(name: String) = providers.environmentVariable(name)
        .orNull
        ?.equals("true")
        ?: false

    if (isEnabled("TARGET_IOS")) {
        targetIos()
    }
    if (isEnabled("TARGET_JS")) {
        targetJs()
    }
    if (isEnabled("TARGET_ANDROID")) {
        targetAndroid()
    }
    targetJvm()

    serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
    // add("commonMainApi", project(":korge-dragonbones"))
}
