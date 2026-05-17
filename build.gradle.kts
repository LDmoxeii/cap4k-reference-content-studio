import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.tasks.Sync
import java.nio.file.Paths

plugins {
    alias(libs.plugins.cap4k.pipeline)
    base
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

val contentStudioSchemaPath =
    layout.projectDirectory.file(
        "cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql"
    ).asFile.absolutePath.replace("\\", "/")

cap4k {
    project {
        basePackage.set("com.only4.cap4k.reference.contentstudio")
        domainModulePath.set("cap4k-reference-content-studio-domain")
        applicationModulePath.set("cap4k-reference-content-studio-application")
        adapterModulePath.set("cap4k-reference-content-studio-adapter")
    }
    templates {
        overrideDirs.from("codegen/templates")
        templateConflictPolicies.put("design/api_payload.kt.peb", "OVERWRITE")
        templateConflictPolicies.put("flow/entry.json.peb", "OVERWRITE")
        templateConflictPolicies.put("flow/entry.mmd.peb", "OVERWRITE")
        templateConflictPolicies.put("flow/index.json.peb", "OVERWRITE")
    }
    types {
        registryFile.set("design/types.json")
    }
    sources {
        designJson {
            enabled.set(true)
            files.from("design/design.json")
        }
        db {
            enabled.set(true)
            url.set(
                "jdbc:h2:mem:content-studio-generator;MODE=MySQL;DATABASE_TO_UPPER=false;INIT=RUNSCRIPT FROM '$contentStudioSchemaPath'"
            )
            username.set("sa")
            password.set("secret")
            schema.set("PUBLIC")
            includeTables.set(
                listOf(
                    "content",
                    "media_processing_task",
                    "publication_release_readiness",
                    "paid_publication_task"
                )
            )
            excludeTables.set(emptyList())
        }
        irAnalysis {
            enabled.set(true)
            inputDirs.from(
                "cap4k-reference-content-studio-domain/build/cap4k-code-analysis",
                "cap4k-reference-content-studio-application/build/cap4k-code-analysis",
                "cap4k-reference-content-studio-adapter/build/cap4k-code-analysis"
            )
        }
    }
    generators {
        designCommand {
            enabled.set(true)
        }
        designApiPayload {
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
        designIntegrationEvent {
            enabled.set(true)
        }
        designIntegrationEventSubscriber {
            enabled.set(true)
        }
        designQuery {
            enabled.set(true)
        }
        designQueryHandler {
            enabled.set(true)
        }
        aggregate {
            enabled.set(true)
            artifacts {
                factory.set(true)
                specification.set(false)
                unique.set(false)
            }
        }
        flow {
            enabled.set(true)
        }
    }
    layout {
        flow {
            outputRoot.set("analysis/flows")
        }
    }
}

tasks.register("syncGeneratedSnapshots") {
    group = "verification"
    description = "Sync generated artifact snapshots into src-generated roots."
    dependsOn(tasks.named("cap4kGenerateSources"))
    dependsOn(
        subprojects.map { project ->
            project.tasks.named("syncGeneratedSnapshots")
        }
    )
}

tasks.register("normalizeAnalysisFlowIndex") {
    group = "verification"
    description = "Normalize committed analysis flow index metadata."
    outputs.upToDateWhen { false }

    doLast {
        val indexFile = layout.projectDirectory.file("analysis/flows/index.json").asFile
        if (!indexFile.isFile) {
            return@doLast
        }

        @Suppress("UNCHECKED_CAST")
        val root = JsonSlurper().parse(indexFile) as MutableMap<String, Any?>
        val projectPath = layout.projectDirectory.asFile.toPath()
        val inputDirs =
            (root["inputDirs"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.map { value ->
                    val path = Paths.get(value)
                    if (path.isAbsolute && path.startsWith(projectPath)) {
                        projectPath.relativize(path).toString().replace("\\", "/")
                    } else {
                        value.replace("\\", "/")
                    }
                }
                ?: emptyList()
        root["inputDirs"] = inputDirs
        indexFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(root)) + System.lineSeparator())
    }
}

tasks.named("cap4kAnalysisGenerate") {
    finalizedBy(tasks.named("normalizeAnalysisFlowIndex"))
}

subprojects {
    val generatedKotlinSourcesDir = layout.buildDirectory.dir("generated/cap4k/main/kotlin")
    tasks.register<Sync>("syncGeneratedSnapshots") {
        group = "verification"
        description = "Sync generated artifact snapshots into src-generated/main/kotlin."
        dependsOn(rootProject.tasks.named("cap4kGenerateSources"))
        from(generatedKotlinSourcesDir)
        into(layout.projectDirectory.dir("src-generated/main/kotlin"))
        includeEmptyDirs = false
    }
}
