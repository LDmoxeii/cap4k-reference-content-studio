pluginManagement {
    repositories {
        maven {
            name = "AliYunCap4k"
            url = uri("https://packages.aliyun.com/67053c6149e9309ce56b9e9e/maven/cap4k")
            credentials {
                username = providers.gradleProperty("aliyun.maven.username").orNull ?: "defaultUsername"
                password = providers.gradleProperty("aliyun.maven.password").orNull ?: "defaultPassword"
            }
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
