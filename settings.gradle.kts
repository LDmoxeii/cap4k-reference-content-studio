pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "cap4k-reference-content-studio"

include("cap4k-reference-content-studio-domain")
include("cap4k-reference-content-studio-application")
include("cap4k-reference-content-studio-adapter")
include("cap4k-reference-content-studio-start")
