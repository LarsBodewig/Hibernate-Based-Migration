package dev.bodewig.java_based_migration.plugin;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.Xpp3Dom;

@Mojo(name = "schedule-info", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.VALIDATE)
public class ScheduleInfoMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject mavenProject;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
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
}
