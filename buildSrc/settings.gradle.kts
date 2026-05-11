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
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
        gradlePluginPortal {
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal {
            content {
                includeGroup("com.only4")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
        gradlePluginPortal {
            content {
                excludeGroupByRegex("com\\.only4(\\..*)?")
            }
        }
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "buildSrc"
