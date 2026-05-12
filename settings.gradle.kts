pluginManagement {
    repositories {
        mavenLocal {
            content {
                includeGroup("com.only4")
                includeGroup("com.only4.cap4k.plugin.pipeline")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "cap4k-reference-content-studio"

include("cap4k-reference-content-studio-domain")
include("cap4k-reference-content-studio-application")
include("cap4k-reference-content-studio-adapter")
include("cap4k-reference-content-studio-start")
