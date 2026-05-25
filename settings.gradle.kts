pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// [cap4k-bootstrap:managed-begin:root-host]
rootProject.name = "cap4k-reference-content-studio"

include(":cap4k-reference-content-studio-domain")
include(":cap4k-reference-content-studio-application")
include(":cap4k-reference-content-studio-adapter")
include(":cap4k-reference-content-studio-start")
// [cap4k-bootstrap:managed-end:root-host]
