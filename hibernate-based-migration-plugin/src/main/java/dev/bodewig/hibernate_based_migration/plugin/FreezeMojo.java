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
		File resourcesOutputDir = this.getResourcesOutputDir(version);
		log.info("Configured resources output directory " + resourcesOutputDir.getAbsolutePath());
		freezeResources(log, resourcesFiles, resourcesOutputDir, pkgName.left(), pkgName.right());
	}

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

	protected void freezeResources(Log log, List<File> from, File to, String oldPkg, String newPkg)
			throws MojoExecutionException {
		try {
			List<File> filterExclude = getResourceFilterungExclude();
			for (File file : from) {
				log.debug("Copying resource " + file.getAbsolutePath());
				File baseDir = file.isDirectory() ? file.getParentFile() : file;
				copyFile(baseDir, file, to, filterExclude, oldPkg, newPkg);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to copy resource file", e);
		}
	}

	protected File getJavaOuputDir(String version) {
		File javaOutputDir = new File(this.frozenDir, version + "/java/");
		javaOutputDir.mkdirs();
		return javaOutputDir;
	}

	protected File getResourcesOutputDir(String version) {
		File resourcesOutputDir = new File(this.frozenDir, version + "/resources/");
		resourcesOutputDir.mkdirs();
		return resourcesOutputDir;
	}

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

	protected static void copyFile(File baseDir, File from, File targetBaseDir, List<File> filterExclude, String oldPkg,
			String newPkg) throws IOException {
		if (from.isDirectory()) {
			for (File f : from.listFiles()) {
				copyFile(baseDir, f, targetBaseDir, filterExclude, oldPkg, newPkg);
			}
		} else {
			URI relativeURI = baseDir.toURI().relativize(from.toURI());
			URI toURI = targetBaseDir.toURI().resolve(relativeURI);
			File to = new File(toURI);
			if (filterExclude.contains(from)) {
				String content = FileUtils.readFileToString(from, StandardCharsets.UTF_8);
				content = content.replace(oldPkg, newPkg);
				FileUtils.writeStringToFile(to, content, StandardCharsets.UTF_8);
			} else {
				FileUtils.copyFile(from, to);
			}
		}
	}

	protected static String normalizedFreezeVersion(String version) throws MojoExecutionException {
		return Utils.toJavaIdentifier(version);
	}
}
