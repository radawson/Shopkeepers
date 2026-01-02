plugins {
	id("java-library")
	id("org.checkerframework")
	id("maven-publish")
	id("com.gradleup.shadow")
	id("eclipse")
}

repositories {
	// WorldGuard
	maven {
		url = uri("https://maven.enginehub.org/repo/")
	}
	// Citizens
	maven {
		url = uri("https://maven.citizensnpcs.co/repo")
	}
	// bStats
	maven {
		url = uri("https://repo.codemc.org/repository/maven-public/")
	}
	// Towny
	maven {
		url = uri("https://repo.glaremasters.me/repository/towny/")
	}
	// ServiceIO (replaces Vault)
	maven {
		url = uri("https://repo.thenextlvl.net/releases")
	}
	// Vault API (for compilation - ServiceIO implements Vault Economy interface)
	maven {
		url = uri("https://nexus.hc.to/content/repositories/pub_releases")
	}
	// Towny alternative (needs a different group id), legacy VaultAPI (for compatibility)
	maven {
		url = uri("https://jitpack.io")
	}
}

configurations {
	// Separate configuration for the shaded dependencies to not also include the '*.eea' files.
	create("shaded")
	named("implementation") {
		// Removed from Bukkit in newer versions:
		exclude(group = "commons-lang", module = "commons-lang")
		
		// We use CheckerFramework:
		exclude(group = "com.google.code.findbugs", module = "jsr305")
		// This artifact also includes jsr305, so we exclude it:
		exclude(group = "com.sk89q.worldedit.worldedit-libs", module = "core")
	}
}

dependencies {
	api(project(":shopkeepers-api"))
	// compileOnly: Omits these dependencies from the transitively inherited apiElements and
	// runtimeElements. However, we need to manually add these dependencies to our test dependencies
	// if needed.
	compileOnly(libs.spigot.api) // Includes the Bukkit API
	compileOnly(libs.worldguard.bukkit)
	compileOnly(libs.towny)
	compileOnly(libs.citizens.main) {
		isTransitive = false
	}
	// ServiceIO (replaces Vault - implements Vault Economy interface for compatibility)
	compileOnly(libs.service.io)
	// Vault API for compilation (ServiceIO implements Vault Economy interface)
	compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
		exclude(group = "org.bukkit", module = "bukkit")
		exclude(group = "com.google.guava", module = "guava")
	}
	compileOnly(libs.bstats.bukkit)
	
	add("shaded", libs.bstats.bukkit)
}

tasks.named<ProcessResources>("processResources") {
	inputs.property("pluginVersion", rootProject.extra["pluginVersion"])
	inputs.property("dboUrl", rootProject.findProperty("dboUrl"))
	
	filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
		expand(
			mapOf(
				"pluginVersion" to rootProject.extra["pluginVersion"],
				"dboUrl" to rootProject.findProperty("dboUrl")
			)
		)
	}
}

tasks.named<Jar>("jar") {
	// We only require the output of the shadowJar task.
	isEnabled = false
}

/*
java {
	withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
	configureJarTask(project, this)
}

tasks.named<Javadoc>("javadoc") {
	(options as StandardJavadocDocletOptions).apply {
		addStringOption("Xdoclint:none")
		addStringOption("Xmaxwarns", "1")
	}
}
*/

tasks.named<com.gradleup.shadow.ShadowJar>("shadowJar") {
	configureJarTaskWithMavenMetadata(project, this)
	// No classifier: Replaces the normal classifier-less jar file (if there is one).
	archiveClassifier.set("")
	configurations = listOf(project.configurations["shaded"])
	/*configurations = listOf(project.configurations["compileClasspath"])
	dependencies {
		include(dependency(libs.bstats.bukkit.get()))
	}*/
	relocate("org.bstats.bukkit", "${project.group}.libs.bstats")
}

configureShadowArtifacts(project)

// Copies the project's jars into the build folder of the root project.
tasks.register<Copy>("copyResults") {
	from(tasks.named("shadowJar"))
	into(rootProject.buildDir)
}

tasks.named("assemble") {
	dependsOn(tasks.named("shadowJar"))
	dependsOn(tasks.named("copyResults"))
}

configure<PublishingExtension> {
	publications {
		create<MavenPublication>("mavenJava") {
			configureShadowMavenPublication(project, this)
		}
	}
}

