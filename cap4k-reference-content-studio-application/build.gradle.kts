plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
    implementation(project(":cap4k-reference-content-studio-domain"))
    implementation("com.only4:ddd-core:0.5.0-SNAPSHOT")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation(kotlin("reflect"))
    implementation("org.springframework:spring-context")
}

kotlin {
    jvmToolchain(21)
}
