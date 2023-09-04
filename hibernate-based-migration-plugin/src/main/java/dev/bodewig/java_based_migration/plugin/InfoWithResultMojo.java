package dev.bodewig.java_based_migration.plugin;

import java.util.Arrays;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.maven.InfoMojo;

@Mojo(name = "info-with-result", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class InfoWithResultMojo extends InfoMojo {

	@Override
	protected void doExecute(Flyway flyway) {
		this.getLog().warn("LOCATIONS: " + Arrays.asList(flyway.getConfiguration().getLocations()).stream()
				.map(l -> l.getDescriptor()).toList());
		MigrationInfoService info = flyway.info();
		this.getLog().warn(
				"MIGRATIONS: " + Arrays.asList(info.all()).stream().map(m -> m.getVersion().getVersion()).toList());
		this.mavenProject.setContextValue("flyway-info", info);
	}
}
