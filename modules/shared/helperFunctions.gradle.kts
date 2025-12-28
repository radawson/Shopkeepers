fun configureJarTask(project: Project, jarTask: Jar) {
	val artifactId = project.extra["artifactId"] as? String ?: project.name
	val buildVersion = project.extra["buildVersion"] as? String ?: project.version.toString()
	
	jarTask.inputs.property("group", project.group)
	jarTask.inputs.property("artifactId", artifactId)
	jarTask.inputs.property("buildVersion", buildVersion)
	
	jarTask.archiveBaseName.set(artifactId)
	jarTask.manifest {
		attributes(
			"Implementation-Title" to "${project.group}:$artifactId",
			"Implementation-Version" to buildVersion
		)
	}
}

fun configureJarTaskWithMavenMetadata(project: Project, jarTask: Jar) {
	configureJarTask(project, jarTask)
	
	// If the maven-publish plugin is used, include the generated Maven metadata files into the jar:
	project.plugins.withId("maven-publish") {
		val artifactId = project.extra["artifactId"] as? String ?: project.name
		jarTask.into("META-INF/maven/${project.group}/$artifactId") {
			from(project.tasks.named("generatePomFileForMavenJavaPublication")) {
				rename(".*", "pom.xml")
			}
			from(project.tasks.named("generateMavenPomPropertiesFile"))
		}
	}
}

fun configureShadowArtifacts(project: Project) {
	project.artifacts {
		// Similar to the regular jar, declare the shadow jar as output of the project for any
		// projects that depend on it.
		add("archives", project.tasks.named("shadowJar"))
		add("apiElements", project.tasks.named("shadowJar"))
		add("runtimeElements", project.tasks.named("shadowJar"))
	}
}

fun configureMavenPublication(project: Project, publication: MavenPublication) {
	publication.artifactId = project.extra["artifactId"] as? String ?: project.name
	publication.pom {
		name.set(project.name)
		description.set(project.description)
		url.set(project.findProperty("dboUrl") as? String ?: "")
		scm {
			url.set(project.findProperty("scmUrl") as? String ?: "")
			connection.set(project.findProperty("scmConnection") as? String ?: "")
			developerConnection.set(project.findProperty("scmDeveloperConnection") as? String ?: "")
		}
		// Note: Gradle intentionally ignores and omits repositories from the pom file.
		// https://github.com/gradle/gradle/issues/15932
		// https://github.com/gradle/gradle/issues/8811
		
		// Note: Gradle maps all api dependencies to 'compile' scope and all implementation
		// dependencies to 'runtime' scope (instead of 'provided' scope). Although this does not
		// match the project's compile configuration (since the runtime scope is not considered part
		// of the project's compilation classpath), this is not an issue because the primary purpose
		// of the published pom file is not to configure the build of this project, but only to
		// ensure that any transitive compile and runtime dependencies are declared for consuming
		// projects.
	}
}

fun configureShadowMavenPublication(project: Project, publication: MavenPublication) {
	configureMavenPublication(project, publication)
	// Adding the java component here, instead of the shadow component, ensures that we generate the
	// default pom contents, including entries for all dependencies. The shadow component would omit
	// all dependencies (except those of the 'shadow' configuration), even if we configure the
	// shadowJar task to only include some of the dependencies.
	// However, the published artifacts are overridden to only publish the shadow jar instead.
	publication.from(project.components["java"])
	publication.artifact(project.tasks.named("shadowJar").get())
}

fun disableMavenPublications(project: Project) {
	project.tasks.withType<AbstractPublishToMaven>().configureEach {
		isEnabled = false
	}
}

