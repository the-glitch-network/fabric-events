rootProject.name = "the-glitch-events"

pluginManagement {
    repositories {
        jcenter()
        maven {
            name = "Fabric"
            url = java.net.URI("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
    plugins {
        //Work-around due to plugins block not accepting property("loom_version")
        id("fabric-loom") version System.getProperty("loom_version")!!
    }
}

include("utilities")