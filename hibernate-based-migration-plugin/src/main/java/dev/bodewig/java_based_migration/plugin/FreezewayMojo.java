package dev.bodewig.java_based_migration.plugin;

import dev.bodewig.java_based_migration.plugin.model.FreezewayMojoModel;
import dev.bodewig.java_based_migration.plugin.util.Spoon;
import dev.bodewig.java_based_migration.plugin.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

@Mojo(name = "freezeway", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PROCESS_SOURCES)
@Execute(goal = "info-with-result")
public class FreezewayMojo extends FreezewayMojoModel {

	@Override
	protected void doExecute(Flyway flyway) {
		try {
			MigrationInfoService info = (MigrationInfoService) executedProject.getContextValue("flyway-info");
			if (info == null) {
				this.getLog().warn("INVOKING INFO-WITH-RESULTS");
				invokeInfoWithResult();
			} else {
				this.getLog().warn("FLYWAY-INFO: " + info);
				Optional<String> version = getPendingVersion(info);
				this.getLog().warn("PENDING FLYWAY VERSION: " + version);
				if (version.isEmpty()) {
					return;
				}
				String[] migrations = getPendingMigrations(info);
				freezeEntities(version.get());
				rewriteMigrations(version.get(), migrations);
				// run migrate in later phase?
				// flyway.migrate();
				invokeMigrate();
			}
		} catch (MojoExecutionException e) {
			e.printStackTrace();
		}
	}

	protected void invokeInfoWithResult() {
		PluginExecution info = new PluginExecution();
		info.addGoal("info-with-result");
		Plugin thisPlugin = this.mavenProject
				.getPlugin("dev.bodewig.hibernate-based-migrations:hibernate-based-migration");
		PluginExecution freezeway = null;
		for (PluginExecution pe : thisPlugin.getExecutions()) {
			if (pe.getGoals().contains("freezeway")) {
				freezeway = pe;
			}
		}
		Xpp3Dom dom = (Xpp3Dom) freezeway.getConfiguration();
		info.setConfiguration(dom);
		thisPlugin.addExecution(info);
		this.getLog().warn("INFO-WITH-RESULT CONFIGURED: " + dom);
	}

	protected void invokeMigrate() {
		Plugin migrate = this.mavenProject.getPlugin("org.flywaydb.core:flyway-maven-plugin");
		if (migrate == null) {
			migrate = new Plugin();
			migrate.setGroupId("org.flywaydb.core");
			migrate.setArtifactId("flyway-maven-plugin");
			migrate.setVersion("9.22.0");
			this.mavenProject.getBuild().addPlugin(migrate);
		}
		PluginExecution exec = new PluginExecution();
		exec.addGoal("migrate");
		Plugin thisPlugin = this.mavenProject
				.getPlugin("dev.bodewig.hibernate-based-migrations:hibernate-based-migration");
		PluginExecution freezeway = null;
		for (PluginExecution pe : thisPlugin.getExecutions()) {
			if (pe.getGoals().contains("freezeway")) {
				freezeway = pe;
			}
		}
		Xpp3Dom dom = (Xpp3Dom) freezeway.getConfiguration();
		exec.setConfiguration(dom);
		migrate.addExecution(exec);
	}

	protected Optional<String> getPendingVersion(MigrationInfoService info) {
		MigrationInfo[] pending = info.pending();
		if (pending.length > 0) {
			Arrays.sort(pending);
			MigrationInfo highest = pending[pending.length - 1];
			MigrationVersion version = highest.getVersion();
			String versionStr = version.getVersion();
			return Optional.of(versionStr);
		}
		return Optional.empty();
	}

	protected String[] getPendingMigrations(MigrationInfoService info) {
		MigrationInfo[] pending = info.pending();
		String[] migrations = new String[pending.length];
		for (int i = 0; i < pending.length; i++) {
			migrations[i] = pending[i].getPhysicalLocation();
		}
		return migrations;
	}

	protected void freezeEntities(String version) throws MojoExecutionException {
		Log log = this.getLog();
		Spoon spoon = new Spoon(log);
		List<File> classesFiles = this.getPersistenceClasses();
		spoon.addClasses(classesFiles);
		// identify persistence base package
		// add migrations to model
		// get references to persistence classes
		// rename persistence package
		// write persistence classes
	}

	protected void rewriteMigrations(String version, String[] migrations) {
		// update references to persistence classes
		// write migrations
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

	protected File getJavaOuputDir(String version) {
		File javaOutputDir = new File(this.frozenDir, version + "/java/");
		javaOutputDir.mkdirs();
		return javaOutputDir;
	}
}
