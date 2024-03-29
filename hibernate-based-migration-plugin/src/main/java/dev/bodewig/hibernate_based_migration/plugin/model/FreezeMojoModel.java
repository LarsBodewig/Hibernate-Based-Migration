package dev.bodewig.hibernate_based_migration.plugin.model;

import dev.bodewig.hibernate_based_migration.plugin.util.Glob;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Model for the {@link dev.bodewig.hibernate_based_migration.plugin.FreezeMojo
 * FreezeMojo}
 */
public abstract class FreezeMojoModel extends AbstractMojo {

	/**
	 * Default constructor
	 */
	public FreezeMojoModel() {
	}

	/**
	 * The version used if no prior version is found or configured
	 */
	protected static final String INITIAL_VERSION = "1";

	/**
	 * The version used to store frozen files and to calculate the base package name
	 * <p>
	 * Usually this should be an continuously increasing integer. If no version is
	 * supplied the {@code frozenDir} is inspected for existing versions and tries
	 * to increment. If no prior version is found starts with version 1.
	 */
	@Parameter
	protected String freezeVersion;

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

	/**
	 * Define resources to freeze (can be used in conjunction with {@code
	 * persistenceResourcesGlobList})
	 *
	 * <pre>
	 * &lt;persistenceResourcesFileList&gt;
	 * 	&lt;file&gt;src/main/resources/logging.xml&lt;/file&gt;
	 * 	&lt;file&gt;src/main/resources/config&lt;/file&gt;
	 * 	...
	 * &lt;/persistenceResourcesFileList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<File> persistenceResourcesFileList;

	/**
	 * Define resources to freeze (can be used in conjunction with {@code
	 * persistenceResourcesFileList})
	 *
	 * <pre>
	 * &lt;persistenceResourcesGlobList&gt;
	 * 	&lt;glob&gt;path/relative/to/./src/main/resources/*.xml_bak&lt;/glob&gt;
	 * 	...
	 * 	&lt;glob&gt;
	 * 		&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;
	 * 		&lt;pattern&gt;*.conf&lt;/pattern&gt;
	 * 	&lt;/glob&gt;
	 * &lt;/persistenceResourcesGlobList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<Glob> persistenceResourcesGlobList;

	/**
	 * List of {@code persistenceResourcesFiles} that should not be considered when
	 * replacing package references (can be used in conjunction with {@code
	 * resourceFilteringExcludeGlobList})
	 * 
	 * <pre>
	 * &lt;resourceFilteringExcludeFileList&gt;
	 * 	&lt;file&gt;src/main/resources/logging.xml&lt;/file&gt;
	 * 	&lt;file&gt;src/main/resources/config&lt;/file&gt;
	 * 	...
	 * &lt;/resourceFilteringExcludeFileList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<File> resourceFilteringExcludeFileList;

	/**
	 * List of {@code persistenceResourcesGlobs} that should not be considered when
	 * replacing package references (can be used in conjunction with {@code
	 * resourceFilteringExcludeFileList})
	 * 
	 * <pre>
	 * &lt;resourceFilteringExcludeGlobList&gt;
	 * 	&lt;glob&gt;path/relative/to/./src/main/resources/*.xml_bak&lt;/glob&gt;
	 * 	...
	 * 	&lt;glob&gt;
	 * 		&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;
	 * 		&lt;pattern&gt;*.conf&lt;/pattern&gt;
	 * 	&lt;/glob&gt;
	 * &lt;/resourceFilteringExcludeGlobList&gt;
	 * </pre>
	 */
	@Parameter
	protected List<Glob> resourceFilteringExcludeGlobList;
}
