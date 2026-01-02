plugins {
	id("java-library")
	id("org.checkerframework")
	id("maven-publish")
	id("eclipse")
}

configurations {
	named("implementation") {
		// Removed from Bukkit in newer versions:
		exclude(group = "commons-lang", module = "commons-lang")
	}
}

dependencies {
	// compileOnly: Omits the dependency from the transitively inherited apiElements and
	// runtimeElements. However, we need to manually add these dependencies to our test dependencies
	// if needed.
	// Although consumers of this library require the Bukkit dependency, we expect them to declare
	// Bukkit as one of their own dependencies anyway. If they require a more specific version or
	// variant of Bukkit, our transitive Bukkit dependency would likely conflict with that and would
	// then need to be manually ignored.
	// We depend on the Spigot API here, instead of Bukkit, even though we only actually require
	// Bukkit, because the Spigot repository does not contain Bukkit for certain versions. And we
	// also run into issues when we try to build Bukkit/Spigot as part of the Jitpack build.
	compileOnly(libs.spigot.api)
	// For some reason, this needs to use the 'api' configuration in order for Eclipse to properly
	// resolve our own JDK EEAs. compileOnly and compileOnlyApi are not sufficient, neither here nor
	// in the dependent projects.
	api(project(":shopkeepers-external-annotations"))
}

java {
	withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
	configureJarTask(project, this)
}

check {
	// Test compilation against Paper API:
	dependsOn compileWithPaper
}

// Copies the project's jars into the build folder of the root project.
tasks.register<Copy>("copyResults") {
	from(tasks.named("jar"))
	from(tasks.named("javadocJar"))
	into(rootProject.layout.buildDirectory.get().asFile)
}

tasks.named("assemble") {
	dependsOn(tasks.named("copyResults"))
}

configure<PublishingExtension> {
	publications {
		create<MavenPublication>("mavenJava") {
			configureMavenPublication(project, this)
			from(components["java"])
		}
	}
}

