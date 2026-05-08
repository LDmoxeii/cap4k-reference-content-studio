plugins {
    base
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

tasks.register("syncGeneratedSnapshots") {
    group = "verification"
    description = "Sync generated artifact snapshots into src-generated roots."
}
