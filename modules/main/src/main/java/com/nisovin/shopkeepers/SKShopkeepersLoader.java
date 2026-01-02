package com.nisovin.shopkeepers;

import org.checkerframework.checker.nullness.qual.NonNull;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

/**
 * Loader for the Shopkeepers Paper plugin.
 * <p>
 * Configures the plugin's classpath. Currently minimal, but provides structure
 * for future classpath configuration needs.
 */
public class SKShopkeepersLoader implements PluginLoader {

	@Override
	public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
		// Currently no additional libraries need to be added to the classpath.
		// All dependencies are already included via the build system.
		// This structure allows for future classpath configuration if needed.
		//
		// Example for adding a Maven library:
		// MavenLibraryResolver resolver = new MavenLibraryResolver();
		// resolver.addDependency(new Dependency(
		//     new DefaultArtifact("com.example:example:version"), null));
		// resolver.addRepository(new RemoteRepository.Builder("paper", "default",
		//     "https://repo.papermc.io/repository/maven-public/").build());
		// classpathBuilder.addLibrary(resolver);
	}
}

