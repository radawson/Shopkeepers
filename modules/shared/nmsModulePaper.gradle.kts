plugins {
	id("java-library")
	id("org.checkerframework")
	id("eclipse")
	id("io.papermc.paperweight.userdev")
}

// Every project using this script must declare this version BEFORE applying this script:
/*extra {
	craftbukkitVersion = "UNSPECIFIED"
}*/

if (!project.hasProperty("craftbukkitVersion")) {
	throw GradleException("Property 'craftbukkitVersion' must be defined before applying this script.")
}

repositories {
	// Paper
	maven {
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
}

dependencies {
	implementation(project(":shopkeepers-main"))
	paperweight.paperDevBundle(project.findProperty("craftbukkitVersion") as String) // Includes CraftBukkit and PaperAPI
	
	testImplementation(libs.junit)
	testImplementation(libs.asm)
}

// Paper 1.20.5+ uses Mojang mappings at runtime, so we use Mojang-mapped dev jar directly
// No remapping needed - compile with Mojang, run with Mojang

afterEvaluate {
	configurations {
		create("remapped") {
			isCanBeConsumed = true
			isCanBeResolved = false
			attributes {
				attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
				attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
			}
		}
	}
	
	artifacts {
		// Use Mojang-mapped dev jar directly (Paper uses Mojang at runtime)
		add("remapped", tasks.named("jar").map { it.archiveFile })
	}
}

// Note: The compat modules are not published as standalone artifacts, but are shaded into the final plugin jar.

