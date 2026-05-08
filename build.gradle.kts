plugins {
    id("com.only4.cap4k.plugin.pipeline") version "0.5.0-SNAPSHOT"
    base
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

val contentStudioSchemaPath =
    layout.projectDirectory.file(
        "cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql"
    ).asFile.absolutePath.replace("\\", "/")
val contentStudioDbPath =
    layout.buildDirectory.file("h2/content-studio").get().asFile.absolutePath.replace("\\", "/")

cap4k {
    project {
        basePackage.set("com.only4.cap4k.reference.contentstudio")
        domainModulePath.set("cap4k-reference-content-studio-domain")
        applicationModulePath.set("cap4k-reference-content-studio-application")
        adapterModulePath.set("cap4k-reference-content-studio-adapter")
    }
    sources {
        designJson {
            enabled.set(true)
            files.from("design/design.json")
        }
        db {
            enabled.set(true)
            url.set(
                "jdbc:h2:file:$contentStudioDbPath;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;INIT=RUNSCRIPT FROM '$contentStudioSchemaPath'"
            )
            username.set("sa")
            password.set("secret")
            schema.set("PUBLIC")
            includeTables.set(listOf("content", "media_processing_task"))
            excludeTables.set(emptyList())
        }
    }
    generators {
        designCommand {
            enabled.set(true)
        }
        designQuery {
            enabled.set(true)
        }
        designQueryHandler {
            enabled.set(true)
        }
        designClient {
            enabled.set(true)
        }
        designClientHandler {
            enabled.set(true)
        }
        designDomainEvent {
            enabled.set(true)
        }
        designDomainEventHandler {
            enabled.set(true)
        }
        aggregate {
            enabled.set(true)
            artifacts {
                factory.set(true)
                specification.set(true)
                unique.set(false)
                enumTranslation.set(false)
            }
        }
    }
}

tasks.register("syncGeneratedSnapshots") {
    group = "verification"
    description = "Sync generated artifact snapshots into src-generated roots."
}
