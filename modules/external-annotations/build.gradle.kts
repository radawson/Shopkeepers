plugins {
	id("java-library")
	id("org.checkerframework")
	id("maven-publish")
	id("eclipse")
}

dependencies {
	compileOnlyApi(libs.jdt.annotations)
	// no-npe dependency commented out - repository unavailable (https://raw.githubusercontent.com/vegardit/no-npe/mvn-snapshots-repo)
	// This is optional for null analysis annotations and not required for the build
	// compileOnlyApi(libs.nonpe.java21)
	// Include the 'src/main/resources' contents on the compile classpath so that the ECJ compiler
	// finds the external annotations when invoked from Gradle with "-annotationpath CLASSPATH":
	compileOnlyApi(files("src/main/resources"))
	compileOnly(libs.spigot.api) // Includes the Bukkit API
	
	testImplementation(libs.spigot.api)
	testImplementation(libs.junit)
}

// Copies the project's jars into the build folder of the root project.
tasks.register<Copy>("copyResults") {
	from(tasks.named("jar"))
	into(rootProject.buildDir)
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

