package dev.bodewig.hibernate_based_migration;

import java.io.File;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.extensibility.MigrationType;

public abstract class HibernateMigration extends BaseJavaMigration {

	protected File configOld;

	protected File configNew;

	@Override
	protected void init() {
		super.init();
		Migration migration = this.getClass().getAnnotation(Migration.class);
		this.configOld = new File(migration.fromCfg());
		this.configNew = new File(migration.toCfg());
	}

	@Override
	public void migrate(Context context) throws Exception {
		// TODO Auto-generated method stub

		/*
		 * try (Statement select = context.getConnection().createStatement()) { try
		 * (ResultSet rows = select.executeQuery("SELECT id FROM person ORDER BY id")) {
		 * while (rows.next()) { int id = rows.getInt(1); String anonymizedName =
		 * "Anonymous" + id; try (Statement update =
		 * context.getConnection().createStatement()) {
		 * update.execute("UPDATE person SET name='" + anonymizedName + "' WHERE id=" +
		 * id); } } } }
		 */
	}

	@Override
	public MigrationType getType() {
		return CoreMigrationType.CUSTOM;
	}
}
