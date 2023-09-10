# Hibernate-Based-Migration

Hibernate Based Migrations are a simple method to manage migrations of JPA-generated classes or other files between application versions. It offers goals to *freeze* files in a specific version and *thaw* them in a later version to migrate persisted data using Hibernate. This allows more flexibility than simple database migration tools using SQL scripts.

## Usage

1. Add the _hibernate-based-migration-maven-plugin_ to your project
2. Add the **thaw** goal to your project lifecycle
3. Add and execute the **freeze** goal to copy persistence files
4. Change your persistence files
5. Call the **freeze** goal again (you now have 2 frozen versions)
6. Add a dependency to _hibernate-based-migration_
7. Subclass **HibernateMigration** to create a migration between the frozen version of your persistence files (read the Hibernate configuration from the frozen version directory)
8. Add the _flyway-maven-plugin_ to your project
9. Execute the **migrate** goal to run your migration class
10. Profit!


## Goals

* **Freeze (defaultPhase=GenerateSources):**

  Copy a set of files to a separate project directory, resolve the common package and place them in a versioned, named package.

  | Configuration option | Description | Default/Required | Example |
  | --- | --- | --- | --- |
  | freezeVersion | The version used to store frozen files and to calculate the base package name | If no version is supplied the frozenDir is inspected for existing versions and tries to increment. If no prior version is found starts with version 1 | `1` |
  | frozenDir | Output directory for the frozen persistence files | `${project.basedir}/src/migration/` | `${project.basedir}/migration/` |
  | persistenceClassesFileList | Define persistence classes to freeze (can be used in conjunction with persistenceClassesGlobList) | Requires one of persistenceClassesFileList, persistenceClassesGlobList | &lt;persistenceClassesFileList&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/java/my/db/classes/Person.java&lt;/file&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/java/my/db/classes/pkg&lt;/file&gt;<br>&nbsp;&nbsp;...<br>&lt;/persistenceClassesFileList&gt; |
  | persistenceClassesGlobList | Define persistence classes to freeze (can be used in conjunction with persistenceClassesFileList) | Requires one of persistenceClassesFileList, persistenceClassesGlobList | &lt;persistenceClassesGlobList&gt;<br>&nbsp;&nbsp;&lt;glob&gt;path/relative/to/./src/main/java/my/db/classes/C\*.java&lt;/glob&gt;<br>&nbsp;&nbsp;...<br>&nbsp;&nbsp;&lt;glob&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;basePath&gt;${project.build.sourceDirectory}&lt;/basePath&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;pattern&gt;Bi\*.java&lt;/pattern&gt;<br>&nbsp;&nbsp;&lt;/glob&gt;<br>&lt;/persistenceClassesGlobList&gt; |
  | persistenceResourcesFileList | Define resources to freeze (can be used in conjunction with persistenceResourcesGlobList) | Requires one of persistenceResourcesFileList, persistenceResourcesGlobList | &lt;persistenceResourcesFileList&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/resources/logging.xml&lt;/file&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/resources/config&lt;/file&gt;<br>&nbsp;&nbsp;...<br>&lt;/persistenceResourcesFileList&gt; |
  | persistenceResourcesGlobList | Define resources to freeze (can be used in conjunction with persistenceResourcesFileList) | Requires one of persistenceResourcesFileList, persistenceResourcesGlobList | &lt;persistenceResourcesGlobList&gt;<br>&nbsp;&nbsp;&lt;glob&gt;path/relative/to/./src/main/resources/\*.xml_bak&lt;/glob&gt;<br>&nbsp;&nbsp;...<br>&nbsp;&nbsp;&lt;glob&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;pattern&gt;\*.conf&lt;/pattern&gt;<br>&nbsp;&nbsp;&lt;/glob&gt;<br>&lt;/persistenceResourcesGlobList&gt; |
  | resourceFilteringExcludeFileList | List of persistenceResourcesFiles that should not be considered when replacing package references (can be used in conjunction with resourceFilteringExcludeGlobList) | Defaults to empty list | &lt;resourceFilteringExcludeFileList&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/resources/logging.xml&lt;/file&gt;<br>&nbsp;&nbsp;&lt;file&gt;src/main/resources/config&lt;/file&gt;<br>&nbsp;&nbsp;...<br>&lt;/resourceFilteringExcludeFileList&gt; |
  | resourceFilteringExcludeGlobList | List of persistenceResourcesGlobs that should not be considered when replacing package references (can be used in conjunction with resourceFilteringExcludeGlobList) | Defaults to empty list | &lt;resourceFilteringExcludeGlobList&gt;<br>&lt;glob&gt;path/relative/to/./src/main/resources/*.xml_bak&lt;/glob&gt;<br>&nbsp;&nbsp;...<br>&nbsp;&nbsp;&lt;glob&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;pattern&gt;*.conf&lt;/pattern&gt;<br>&nbsp;&nbsp;&lt;/glob&gt;<br>&lt;/resourceFilteringExcludeGlobList&gt; |

* **Thaw (defaultPhase=Initialize):**

  Target a range of versioned frozen files to include them for compilation.

  | Configuration option | Description | Default/Required | Example |
  | --- | --- | --- | --- |
  | frozenDir | Directory of the frozen persistence files | `${project.basedir}/src/migration/` | `${project.basedir}/migration/` |
  | versionRange | Frozen versions to include as project source. Uses the default maven version range format. | Defaults to all versions | `[15,)` |

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
