[![Available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.hibernate-based-migration/hibernate-based-migration?label=Available%20on%20Maven%20Central)](https://central.sonatype.com/namespace/dev.bodewig.hibernate-based-migration)

# Hibernate-Based-Migration

Hibernate Based Migrations are a ~~simple~~ method to do complex migrations of persisted data using Hibernate. Since Hibernate mappings and annotated entity classes always represent the current state of the persisted data, the Maven plugin offers goals to _freeze_ files in a specific version and _thaw_ them in a later version to use in a Migration class. Hibernate Migrations leverage Flyway's rarely used Java-based-Migrations feature.

## Usage

### 1. Add the _hibernate-based-migration-plugin_ to your project

```xml
<plugin>
    <groupId>dev.bodewig.hibernate-based-migration</groupId>
    <artifactId>hibernate-based-migration-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

### 2. Add the **thaw** goal to your project lifecycle

```xml
<plugin>
    ...
    <executions>
        <execution>
            <id>thaw</id>
            <goals>
                <goal>thaw</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 3. Add and execute the **freeze** goal to copy persistence files

```xml
<plugin>
    ...
    <executions>
        <execution>
            <id>freeze</id>
            <goals>
                <goal>freeze</goal>
            </goals>
            <configuration>
                <persistenceClassesFileList>
                    <file>src/main/java/dev/bodewig/hibernate_based_migration/example/Fruit.java</file>
                </persistenceClassesFileList>
                <persistenceResourcesFileList>
                    <file>src/main/resources</file>
                </persistenceResourcesFileList>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 4. Change your persistence files

```java
@Entity
public class Fruit {
	@Id
	public String name;
	public int weight;
	public String colorHex; // --> public String colorRgb;
}
```

### 5. Call the **freeze** goal again (you now have 2 frozen versions)

```
src
|- main
|- migration
   |- 1
   |  |- java/...
   |  |- resources/...
   |- 2
      |- java/...
      |- resources/...
```

### 6. Add a dependency to _hibernate-based-migration_

```xml
<dependency>
    <groupId>dev.bodewig.hibernate-based-migration</groupId>
    <artifactId>hibernate-based-migration</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 7. Subclass **HibernateMigration** to create a migration between the frozen versions of your persistence files (read the Hibernate configuration from the frozen version directories)

```java
public class V1__My_Migration extends HibernateMigration {

	private List<OldData> persistedData;

	@Override
	protected long getSerialVersionUID() { ... }

	@Override
	public Configuration configBefore() {
		return new Configuration().configure("/_1/hibernate.cfg.xml");
	}

	@Override
	public Configuration configAfter() {
		return new Configuration().configure("/_2/hibernate.cfg.xml");
	}

	@Override
	public void doBefore(StatelessSession session) {
		this.persistedData = loadDataWithHibernate(session);
	}

	@Override
	public void doSql(Context context) throws SQLException {
		try (Statement stmt = context.getConnection().createStatement()) {
			// change your database scheme
		}
	}

	@Override
	public void doAfter(StatelessSession session) {
		NewData data = migrateData(this.persistedData);
		saveDataWithHibernate(data);
	}
}
```


### 8. Add the _flyway-maven-plugin_ to your project

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>9.22.0</version>
</plugin>
```

### 9. Execute the **migrate** goal to run your migration class

```xml
<plugin>
    ...
    <executions>
        <execution>
            <id>migrate</id>
            <goals>
                <goal>migrate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 10. Profit!

Your database scheme and your persisted data have been migrated - without doing complex value transformations in pure SQL.

A full example implementation is available in the *hibernate-based-migration-example*.


## Hibernate-Based-Migration-Plugin Goals

### Freeze (defaultPhase=GenerateSources,Execute=Compile)

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
  | resourceFilteringExcludeGlobList | List of persistenceResourcesGlobs that should not be considered when replacing package references (can be used in conjunction with resourceFilteringExcludeGlobList) | Defaults to empty list | &lt;resourceFilteringExcludeGlobList&gt;<br>&lt;glob&gt;path/relative/to/./src/main/resources/\*.xml_bak&lt;/glob&gt;<br>&nbsp;&nbsp;...<br>&nbsp;&nbsp;&lt;glob&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;basePath&gt;${project.build.resourceDirectory}&lt;/basePath&gt;<br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;pattern&gt;\*.conf&lt;/pattern&gt;<br>&nbsp;&nbsp;&lt;/glob&gt;<br>&lt;/resourceFilteringExcludeGlobList&gt; |

## Thaw (defaultPhase=Initialize)

  Target a range of versioned frozen files to include them for compilation.

  | Configuration option | Description | Default/Required | Example |
  | --- | --- | --- | --- |
  | frozenDir | Directory of the frozen persistence files | `${project.basedir}/src/migration/` | `${project.basedir}/migration/` |
  | versionRange | Frozen versions to include as project source. Uses the default maven version range format. | Defaults to all versions | `[15,)` |

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
