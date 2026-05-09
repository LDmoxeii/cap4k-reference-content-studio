package buildsrc.convention

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

kotlin {
    jvmToolchain(17)
}

val cap4kCompilerPluginClasspath = configurations.create("cap4kCompilerPluginClasspath")
cap4kCompilerPluginClasspath.isCanBeConsumed = false
cap4kCompilerPluginClasspath.isCanBeResolved = true
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    cap4kCompilerPluginClasspath(libs.findLibrary("cap4k-plugin-code-analysis-compiler").get())
    cap4kCompilerPluginClasspath(libs.findLibrary("cap4k-plugin-code-analysis-core").get())
}
val cap4kPluginArgs = providers.provider {
    cap4kCompilerPluginClasspath
        .resolve()
        .map { "-Xplugin=${it.absolutePath}" }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.freeCompilerArgs.addAll(cap4kPluginArgs)
    outputs.dir(layout.buildDirectory.dir("cap4k-code-analysis"))
}
