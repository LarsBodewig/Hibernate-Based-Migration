package dev.bodewig.hibernate_based_migration.plugin.model;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Model for the {@link dev.bodewig.hibernate_based_migration.plugin.ThawMojo
 * ThawMojo}
 */
public abstract class ThawMojoModel extends AbstractMojo {

	/**
	 * Default constructor
	 */
	public ThawMojoModel() {
	}

	/** Directory of the frozen persistence files */
	@Parameter(defaultValue = "${project.basedir}/src/migration/")
	protected File frozenDir;

	/**
	 * The maven project, readonly
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	/**
	 * Frozen versions to include as project source. Uses the default maven version
	 * range format.
	 * <p>
	 * Defaults to all versions
	 */
	@Parameter
	protected String versionRange;
}
