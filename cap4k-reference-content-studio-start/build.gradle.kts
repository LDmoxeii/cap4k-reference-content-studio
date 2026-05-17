plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":cap4k-reference-content-studio-domain"))
    implementation(project(":cap4k-reference-content-studio-application"))
    implementation(project(":cap4k-reference-content-studio-adapter"))
    implementation(libs.cap4k.ddd.starter)
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.springdoc.webmvc.ui)
    runtimeOnly(libs.ddd.integration.event.http)
    runtimeOnly(libs.ddd.integration.event.http.jpa)
    runtimeOnly(libs.h2)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    forkEvery = 1
}
