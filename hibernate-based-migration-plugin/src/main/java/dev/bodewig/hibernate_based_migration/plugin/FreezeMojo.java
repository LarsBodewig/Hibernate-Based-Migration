package dev.bodewig.hibernate_based_migration.plugin;

import dev.bodewig.hibernate_based_migration.plugin.model.FreezeMojoModel;
import dev.bodewig.hibernate_based_migration.plugin.util.Pair;
import dev.bodewig.hibernate_based_migration.plugin.util.Spoon;
import dev.bodewig.hibernate_based_migration.plugin.util.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Offers functionality to freeze the configured persistence classes to create
 * Hibernate based migrations between different versions of a persistence layer.
 */
@Mojo(name = "freeze", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(phase = LifecyclePhase.COMPILE)
public class FreezeMojo extends FreezeMojoModel {

	/**
	 * Called by Plexus
	 */
	public FreezeMojo() {
	}

	/**
	 * Freeze the configured Java and resource files, rewrite package names and
	 * apply filtering on resources
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = this.getLog();
		Spoon spoon = new Spoon(log);
		List<File> classesFiles = this.getPersistenceClasses();
		log.info("Configured " + classesFiles.size() + " classes to freeze");
		List<File> resourcesFiles = this.getPersistenceResources();
		log.info("Configured " + resourcesFiles.size() + " resources to freeze");
		String version = this.getFreezeVersion();
		log.info("Freezing version " + version);
		spoon.addClasses(classesFiles);
		log.info("Rewriting packages");
		String normalizedVersion = normalizedFreezeVersion(version);
		Pair<String, String> pkgName = spoon.rewritePackages(normalizedVersion);
		log.info("Writing frozen class files");
		File javaOutputDir = this.getJavaOuputDir(version);
		log.info("Configured java output directory " + javaOutputDir.getAbsolutePath());
		spoon.writeClassModel(javaOutputDir);
		log.info("Writing frozen resources");
		File resourcesOutputDir = this.getResourcesOutputDir(version, normalizedVersion);
		log.info("Configured resources output directory " + resourcesOutputDir.getAbsolutePath());
		this.freezeResources(resourcesFiles, resourcesOutputDir, pkgName.left(), pkgName.right());
	}

	/**
	 * Get the configured or calculated freeze version
	 * 
	 * @return the freeze version
	 * @throws MojoExecutionException
	 *             if guessing the next freeze version fails
	 */
	protected String getFreezeVersion() throws MojoExecutionException {
		if (this.freezeVersion == null || this.freezeVersion.isBlank()) {
			File[] versionDirs = this.frozenDir.listFiles();
			if (versionDirs == null) {
				return INITIAL_VERSION;
			} else {
				Integer lastVersion = null;
				for (File versionDir : versionDirs) {
					String dirName = versionDir.getName();
					try {
						int version = Integer.parseInt(dirName);
						if (lastVersion == null || lastVersion.intValue() < version) {
							lastVersion = Integer.valueOf(version);
						}
					} catch (NumberFormatException e) {
						// skip directory
					}
				}
				if (lastVersion == null) {
					throw new MojoExecutionException("Cannot guess next version from existing files");
				}
				int nextVersion = lastVersion.intValue() + 1;
				return Integer.toString(nextVersion);
			}
		} else {
			return this.freezeVersion;
		}
	}

	/**
	 * Freeze the configured list of resources. Does filtering for the transformed
	 * package name inside the resources unless configured to be excluded.
	 * 
	 * @param from
	 *            the list of resources to copy
	 * @param to
	 *            the target directory
	 * @param originalPkg
	 *            the original package name
	 * @param transformedPkg
	 *            the transformed package name
	 * @throws MojoExecutionException
	 *             {@code IOException} during copying
	 */
	protected void freezeResources(List<File> from, File to, String originalPkg, String transformedPkg)
			throws MojoExecutionException {
		try {
			this.getLog().info("Filtering resources: <" + originalPkg + "> to <" + transformedPkg + ">");
			List<File> filterExclude = getResourceFilterungExclude();
			for (File file : from) {
				this.getLog().debug("Copying resource " + file.getAbsolutePath());
				File baseDir = file.isDirectory() ? file : file.getParentFile();
				this.copyFile(baseDir, file, to, filterExclude, originalPkg, transformedPkg);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to copy resource file", e);
		}
	}

	/**
	 * Get the configured Java output directory
	 * 
	 * @param version
	 *            the version String to copy to
	 * @return the Java output directory
	 */
	protected File getJavaOuputDir(String version) {
		File javaOutputDir = new File(this.frozenDir, version + "/java/");
		javaOutputDir.mkdirs();
		return javaOutputDir;
	}

	/**
	 * Get the configured resources output directory
	 * 
	 * @param version
	 *            the version String to copy to
	 * @param normalizedVersion
	 *            the normalized version to include in the resource path
	 * @return the resources output directory
	 */
	protected File getResourcesOutputDir(String version, String normalizedVersion) {
		File resourcesOutputDir = new File(this.frozenDir, version + "/resources/" + normalizedVersion);
		resourcesOutputDir.mkdirs();
		return resourcesOutputDir;
	}

	/**
	 * Get the list of Java files to copy
	 * 
	 * @return the list of source files
	 * @throws MojoExecutionException
	 *             if no source was configured
	 */
	protected List<File> getPersistenceClasses() throws MojoExecutionException {
		List<File> persistenceClasses = new ArrayList<>();
		boolean anySet = false;
		if (this.persistenceClassesFileList != null) {
			anySet |= persistenceClasses.addAll(
					Utils.flatMapList(this.persistenceClassesFileList, f -> FileUtils.listFiles(f, null, true)));
		}
		if (this.persistenceClassesGlobList != null) {
			anySet |= persistenceClasses
					.addAll(Utils.flatMapList(this.persistenceClassesGlobList, g -> g.listMatches()));
		}
		if (!anySet) {
			throw new MojoExecutionException("Missing configuration for persistence classes: atleast one of "
					+ "[persistenceClassesFileList, persistenceClassesGlobList] " + "has to be set");
		}
		return persistenceClasses;
	}

	/**
	 * Get the list of configured resources to copy
	 * 
	 * @return the list of resource files
	 */
	protected List<File> getPersistenceResources() {
		List<File> persistenceResources = new ArrayList<>();
		if (this.persistenceResourcesFileList != null) {
			persistenceResources.addAll(
					Utils.flatMapList(this.persistenceResourcesFileList, f -> FileUtils.listFiles(f, null, true)));
		}
		if (this.persistenceResourcesGlobList != null) {
			persistenceResources.addAll(Utils.flatMapList(this.persistenceResourcesGlobList, g -> g.listMatches()));
		}
		return persistenceResources;
	}

	/**
	 * Get the list of configured resources to not filter when copying
	 * 
	 * @return the list of excluded resource files
	 */
	protected List<File> getResourceFilterungExclude() {
		List<File> excludedResources = new ArrayList<>();
		if (this.resourceFilteringExcludeFileList != null) {
			excludedResources.addAll(
					Utils.flatMapList(this.resourceFilteringExcludeFileList, f -> FileUtils.listFiles(f, null, true)));
		}
		if (this.resourceFilteringExcludeGlobList != null) {
			excludedResources.addAll(Utils.flatMapList(this.resourceFilteringExcludeGlobList, g -> g.listMatches()));
		}
		return excludedResources;
	}

	/**
	 * Recursive method copying resource files and applying filtering based on the
	 * original and transformed package name
	 * 
	 * @param fromBaseDir
	 *            the base directory to copy from
	 * @param from
	 *            the source file to copy
	 * @param toBaseDir
	 *            the base directory to copy to
	 * @param filterExclude
	 *            a list of resources that should not be filtered
	 * @param originalPkg
	 *            the original package name
	 * @param transformedPkg
	 *            the transformed package name
	 * @throws IOException
	 *             file level error
	 */
	protected void copyFile(File fromBaseDir, File from, File toBaseDir, List<File> filterExclude, String originalPkg,
			String transformedPkg) throws IOException {
		if (from.isDirectory()) {
			for (File f : from.listFiles()) {
				this.copyFile(fromBaseDir, f, toBaseDir, filterExclude, originalPkg, transformedPkg);
			}
		} else {
			URI relativeURI = fromBaseDir.toURI().relativize(from.toURI());
			URI toURI = toBaseDir.toURI().resolve(relativeURI);
			File to = new File(toURI);
			if (!filterExclude.contains(from)) {
				String content = FileUtils.readFileToString(from, StandardCharsets.UTF_8);
				content = content.replace(originalPkg, transformedPkg);
				FileUtils.writeStringToFile(to, content, StandardCharsets.UTF_8);
			} else {
				FileUtils.copyFile(from, to);
			}
		}
	}

	/**
	 * Create a valid Java identifier from a version String
	 * 
	 * @param version
	 *            the version String
	 * @return a valid Java identifier created from the version String
	 */
	protected static String normalizedFreezeVersion(String version) {
		return Utils.toJavaIdentifier(version);
	}
}
