import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.only4.cap4k.plugin.pipeline.api.BootstrapMode
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

// [cap4k-bootstrap:managed-begin:root-host]
cap4k {
    bootstrap {
        enabled.set(true)
        preset.set("ddd-multi-module")
        conflictPolicy.set("SKIP")
        mode.set(BootstrapMode.IN_PLACE)
        projectName.set("cap4k-reference-content-studio")
        basePackage.set("com.only4.cap4k.reference.contentstudio")
        modules {
            domainModuleName.set("cap4k-reference-content-studio-domain")
            applicationModuleName.set("cap4k-reference-content-studio-application")
            adapterModuleName.set("cap4k-reference-content-studio-adapter")
            startModuleName.set("cap4k-reference-content-studio-start")
        }
        templates {
            preset.set("ddd-default-bootstrap")
        }
    }
}
// [cap4k-bootstrap:managed-end:root-host]

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
        templateConflictPolicies.put("drawing-board/document.json.peb", "OVERWRITE")
        templateConflictPolicies.put("flow/entry.json.peb", "OVERWRITE")
        templateConflictPolicies.put("flow/entry.mmd.peb", "OVERWRITE")
        templateConflictPolicies.put("flow/index.json.peb", "OVERWRITE")
    }
    types {
        registryFile.set("design/types.json")
        enumManifest {
            files.from("design/enums.json")
        }
        valueObjectManifest {
            files.from("design/value-objects.json")
        }
    }
    sources {
        designJson {
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
                    "paid_publication_task"
                )
            )
            excludeTables.set(emptyList())
        }
        irAnalysis {
            inputDirs.from(
                "cap4k-reference-content-studio-domain/build/cap4k-code-analysis",
                "cap4k-reference-content-studio-application/build/cap4k-code-analysis",
                "cap4k-reference-content-studio-adapter/build/cap4k-code-analysis"
            )
        }
    }
    generators {
        aggregate {
            artifacts {
                factory.set(true)
                specification.set(false)
                unique.set(false)
            }
        }
        flow {
        }
        drawingBoard {
        }
    }
    layout {
        flow {
            outputRoot.set("analysis/flows")
        }
        drawingBoard {
            outputRoot.set("analysis/drawing-board")
        }
    }
}

tasks.register("normalizeAnalysisFlowIndex") {
    group = "verification"
    description = "Normalize committed analysis flow metadata."
    outputs.upToDateWhen { false }

    doLast {
        val analysisRoot = layout.projectDirectory.dir("analysis/flows").asFile
        val indexFile = analysisRoot.resolve("index.json")
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

        analysisRoot.walkTopDown()
            .filter { file -> file.isFile && file.extension == "mmd" }
            .forEach { file ->
                val lines = file.readLines()
                val normalizedLines = lines.dropLastWhile(String::isBlank)
                file.writeText(normalizedLines.joinToString(System.lineSeparator()) + System.lineSeparator())
            }
    }
}

tasks.named("cap4kAnalysisGenerate") {
    finalizedBy(tasks.named("normalizeAnalysisFlowIndex"))
}
