package dev.bodewig.java_based_migration.plugin.model;

import dev.bodewig.java_based_migration.plugin.util.Glob;
import java.io.File;
import java.util.List;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.flywaydb.maven.MigrateMojo;

public abstract class FreezewayMojoModel extends MigrateMojo {

	@Parameter(defaultValue = "${executedProject}", readonly = true)
	protected MavenProject executedProject;

	/** Output directory for the frozen persistence files */
	@Parameter(defaultValue = "${project.basedir}/src/migration/")
	protected File frozenDir;

	/**
	 * Define persistence classes to freeze (can be used in conjunction with {@code
	 * persistenceClassesGlobList})
	 *
	 * <pre>
	 * &lt;persistenceClassesFileList&gt;
	 * 	&lt;file&gt;src/main/java/my/db/classes/Person.java&lt;/file&gt;
	 * 	&lt;file&gt;src/main/java/my/db/classes/pkg&lt;/file&gt;
	 * 	...
	 * &lt;/persistenceClassesFileList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<File> persistenceClassesFileList;

	/**
	 * Define persistence classes to freeze (can be used in conjunction with {@code
	 * persistenceClassesFileList})
	 *
	 * <pre>
	 * &lt;persistenceClassesGlobList&gt;
	 * 	&lt;glob&gt;path/relative/to/./src/main/java/my/db/classes/C*.java&lt;/glob&gt;
	 * 	...
	 * 	&lt;glob&gt;
	 * 		&lt;basePath&gt;${project.build.sourceDirectory}&lt;/basePath&gt;
	 * 		&lt;pattern&gt;Bi*.java&lt;/pattern&gt;
	 * 	&lt;/glob&gt;
	 * &lt;/persistenceClassesGlobList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<Glob> persistenceClassesGlobList;
}
