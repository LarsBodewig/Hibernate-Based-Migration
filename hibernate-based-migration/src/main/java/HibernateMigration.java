import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public abstract class HibernateMigration extends BaseJavaMigration {
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
}
