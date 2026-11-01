plugins {
    alias(libs.plugins.kotlin.jvm)
}

// No Android and no network code in here, on purpose: this module is the whole planning, search and safety rule engine.
kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
}
