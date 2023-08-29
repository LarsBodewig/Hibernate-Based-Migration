package dev.bodewig.java_based_migration;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.JavaMigration;

public abstract class AnnotatedJavaMigration implements JavaMigration {
	private MigrationVersion version;
	private String description;

	public AnnotatedJavaMigration() {
		init();
	}

	protected void init() {
		Migration annot = this.getClass().getAnnotation(Migration.class);
		this.version = MigrationVersion.fromVersion(annot.version());
		this.description = annot.description();
	}

	@Override
	public MigrationVersion getVersion() {
		return version;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Integer getChecksum() {
		return null;
	}

	@Override
	public boolean canExecuteInTransaction() {
		return true;
	}
}
