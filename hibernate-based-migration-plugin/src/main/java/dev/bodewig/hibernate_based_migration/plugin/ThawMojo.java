package dev.bodewig.hibernate_based_migration.plugin;

import dev.bodewig.hibernate_based_migration.plugin.model.ThawMojoModel;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "thaw", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ThawMojo extends ThawMojoModel {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		List<File> versionDirs = this.getVersionDirs();
		if (!versionDirs.isEmpty()) {
			this.getLog().info("Thawing versions: "
					+ versionDirs.stream().map(file -> file.getName()).collect(Collectors.joining(", ")));
			thawSources(versionDirs);
			thawResources(versionDirs);
		}
	}

	protected List<File> getVersionDirs() throws MojoExecutionException {
		List<File> selectedVersionDirs = new LinkedList<>();
		File[] versionDirs = this.frozenDir.listFiles();
		if (versionDirs == null) {
			this.getLog().info("FrozenDir does not exist: " + this.frozenDir);
		} else {
			VersionRange versionRange = null;
			if (this.versionRange != null) {
				try {
					versionRange = VersionRange.createFromVersionSpec(this.versionRange);
				} catch (InvalidVersionSpecificationException e) {
					throw new MojoExecutionException("Invalid versionRange: " + this.versionRange, e);
				}
			}
			for (File versionDir : versionDirs) {
				boolean versionSelected = true;
				if (versionRange != null) {
					ArtifactVersion version = new DefaultArtifactVersion(versionDir.getName());
					versionSelected = versionRange.containsVersion(version);
					this.getLog().debug("Frozen version " + version + " in version range: " + versionSelected);
				}
				if (versionSelected) {
					selectedVersionDirs.add(versionDir);
				}
			}
		}
		return selectedVersionDirs;
	}

	protected void thawResources(List<File> versionDirs) {
		for (File versionDir : versionDirs) {
			File resourcesDir = new File(versionDir, "resources/");
			if (resourcesDir.isDirectory()) {
				String path = resourcesDir.getAbsolutePath();
				Resource resource = new Resource();
				resource.setDirectory(path);
				this.getLog().debug("Adding resource directory: " + path);
				this.project.addResource(resource);
			}
		}
	}

	protected void thawSources(List<File> versionDirs) {
		for (File versionDir : versionDirs) {
			File javaDir = new File(versionDir, "java/");
			if (javaDir.isDirectory()) {
				String path = javaDir.getAbsolutePath();
				this.getLog().debug("Adding source directory: " + path);
				this.project.addCompileSourceRoot(path);
			}
		}
	}
}
