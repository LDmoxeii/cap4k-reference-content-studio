plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
    implementation("com.only4:ddd-core:0.5.0-SNAPSHOT")
    implementation("com.only4:ddd-domain-repo-jpa:0.5.0-SNAPSHOT")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.hibernate.orm:hibernate-core")
    testImplementation("org.junit.jupiter:junit-jupiter:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
