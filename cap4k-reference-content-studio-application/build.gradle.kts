plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":cap4k-reference-content-studio-domain"))
    implementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(21)
}
