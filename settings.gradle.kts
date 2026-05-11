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
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
        gradlePluginPortal {
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
    }
}

rootProject.name = "cap4k-reference-content-studio"

include("cap4k-reference-content-studio-domain")
include("cap4k-reference-content-studio-application")
include("cap4k-reference-content-studio-adapter")
include("cap4k-reference-content-studio-start")
