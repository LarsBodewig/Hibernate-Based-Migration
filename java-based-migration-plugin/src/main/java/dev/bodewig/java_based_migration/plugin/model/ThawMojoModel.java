package dev.bodewig.java_based_migration.plugin.model;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class ThawMojoModel extends AbstractMojo {

	/** Directory of the frozen persistence files */
	@Parameter(defaultValue = "${project.basedir}/src/migration/")
	protected File frozenDir;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	/** Frozen versions to include in the default maven version range format */
	@Parameter(required = true)
	protected String versionRange;
}
