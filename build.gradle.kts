import java.util.Properties

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("org.checkerframework") version "0.6.39"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "com.nisovin.shopkeepers"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/") // WorldGuard
    maven("https://maven.citizensnpcs.co/repo") // Citizens
    maven("https://repo.codemc.org/repository/maven-public/") // bStats
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://repo.thenextlvl.net/releases") // ServiceIO
    maven("https://nexus.hc.to/content/repositories/pub_releases") // Vault
    maven("https://jitpack.io")
    mavenLocal()
}

configurations {
    // No global exclusions - we'll exclude only from specific dependencies
}

dependencies {
    // Paper dev bundle includes Paper API (which replaces Spigot API)
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    
    // Eclipse JDT annotations (for null analysis)
    compileOnly("org.eclipse.jdt:org.eclipse.jdt.annotation:2.3.0")
    
    // Optional dependencies (compileOnly - provided at runtime if available)
    // WorldGuard - exclude transitive dependencies to avoid version conflicts
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.2.0")
    compileOnly("net.citizensnpcs:citizens-main:2.0.33-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("net.thenextlvl.services:service-io:2.3.1") // ServiceIO (replaces Vault)
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "com.google.guava", module = "guava")
    }
    compileOnly("org.bstats:bstats-bukkit:1.5")
    
    // Shaded dependencies (included in final JAR)
    implementation("org.bstats:bstats-bukkit:1.5")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceSets.main {
        // Include external-annotations first (for null analysis annotations)
        // Then API source (so main can depend on it)
        // Then main source
        java.srcDirs(
            "modules/external-annotations/src/main/java",
            "modules/api/src/main/java",
            "modules/main/src/main/java"
        )
        resources.srcDirs(
            "modules/external-annotations/src/main/resources",
            "modules/api/src/main/resources",
            "modules/main/src/main/resources"
        )
    }
}

// Add external-annotations resources to compile classpath (for Eclipse External Annotations)
configurations {
    create("annotationPath") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

dependencies {
    // Add external-annotations resources to annotation path
    add("annotationPath", files("modules/external-annotations/src/main/resources"))
}

// Configure paperweight for Mojang-mapped production output
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

// Store version at configuration time
val projectVersion = version.toString()

println("Project version: $version")

/**
 * Task to increment the patch version in gradle.properties.
 */
abstract class IncrementPatchVersionTask : DefaultTask() {
    @get:OutputFile
    abstract val propertiesFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val propsFile = propertiesFile.get().asFile
        if (!propsFile.exists()) {
            throw GradleException("gradle.properties file not found at: ${propsFile.path}")
        }

        val props = Properties()
        propsFile.reader(Charsets.UTF_8).use { reader ->
            props.load(reader)
        }

        val currentVersion = props.getProperty("version")
            ?: throw GradleException("Could not find 'version' property in ${propsFile.path}")
        logger.quiet("Current version from file: $currentVersion")

        val versionRegex = """^(\d+)\.(\d+)\.(\d+)(.*)$""".toRegex()
        val matchResult = versionRegex.find(currentVersion)
            ?: throw GradleException("Version '$currentVersion' does not match expected Major.Minor.Patch format.")

        val (majorStr, minorStr, patchStr, suffix) = matchResult.destructured
        val patch = patchStr.toInt() + 1

        val newVersion = "$majorStr.$minorStr.$patch$suffix"
        logger.quiet("Incremented version to: $newVersion")

        props.setProperty("version", newVersion)
        propsFile.writer(Charsets.UTF_8).use { writer ->
            props.store(writer, null)
        }
    }
}

// Register the task
val incrementPatchVersion = tasks.register<IncrementPatchVersionTask>("incrementPatchVersion") {
    propertiesFile.set(project.layout.projectDirectory.file("gradle.properties"))
    onlyIf { propertiesFile.get().asFile.exists() }
    outputs.upToDateWhen { false }
}

tasks {
    // Configure shadowJar
    shadowJar {
        enableRelocation = false
        archiveClassifier.set("")
        
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }

        // Relocate bStats
        relocate("org.bstats.bukkit", "${project.group}.libs.bstats")
        
        mergeServiceFiles()
    }

    // Configure jar task - disabled since we use shadowJar
    jar {
        isEnabled = false
    }

    clean {
        delete(layout.buildDirectory)
    }
    
    // Process resources
    processResources {
        filesMatching(listOf("plugin.yml")) {
            expand(
                "pluginVersion" to project.version,
                "dboUrl" to (project.findProperty("dboUrl") ?: "")
            )
        }
    }
}

// Configure reobfJar to use shadowJar as input
tasks.named("reobfJar").configure {
    val shadowJar = tasks.named("shadowJar")
    val remapJar = this as io.papermc.paperweight.tasks.RemapJar
    // Declare dependency on shadowJar
    dependsOn(shadowJar)
    remapJar.inputJar.set(
        shadowJar.flatMap { task -> 
            task.outputs.files.singleFile.let { file ->
                layout.file(providers.provider { file })
            }
        }
    )
    // Rename output to -paper
    doLast {
        val outputFile = remapJar.outputJar.get().asFile
        val newFile = File(outputFile.parent, outputFile.name.replace("-reobf.jar", "-paper.jar"))
        if (outputFile.exists() && outputFile != newFile) {
            outputFile.renameTo(newFile)
        }
    }
}

// Ensure the production JAR is built with the assemble task
tasks.assemble {
    dependsOn(tasks.reobfJar)
}

// Ensure the 'build' task runs the increment task AFTER finishing
afterEvaluate {
    tasks.named("build").get().finalizedBy(tasks.named("incrementPatchVersion"))
}
