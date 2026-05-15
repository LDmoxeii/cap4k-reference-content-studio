plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
    implementation(project(":cap4k-reference-content-studio-domain"))
    implementation(project(":cap4k-reference-content-studio-application"))
    implementation(libs.ddd.core)
    implementation(libs.ddd.domain.repo.jpa)
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.springframework:spring-web")
    compileOnly(libs.swagger.annotations.jakarta)

    testImplementation("org.junit.jupiter:junit-jupiter:${libs.versions.junit.get()}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
}
