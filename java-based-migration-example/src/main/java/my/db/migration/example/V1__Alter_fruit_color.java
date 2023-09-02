package my.db.migration.example;

import dev.bodewig.hibernate_based_migration.HibernateMigration;
import java.sql.Statement;
import org.flywaydb.core.api.migration.Context;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

public class V1__Alter_fruit_color extends HibernateMigration {

	private my.db._1_0_0.classes.pkg.food.Fruit data;

	@Override
	public Configuration configBefore() {
		return new Configuration().configure("/db/V0_hibernate.cfg.xml");
	}

	@Override
	public Configuration configAfter() {
		return new Configuration().configure("/db/V1_hibernate.cfg.xml");
	}

	@Override
	public void doBefore(StatelessSession session) throws Exception {
		data = session.get(my.db._1_0_0.classes.pkg.food.Fruit.class, "Orange");
	}

	@Override
	public void doSql(Context context) throws Exception {
		// migrate scheme to 2.0.0
		try (Statement stmt = context.getConnection().createStatement()) {
			stmt.executeUpdate("ALTER TABLE Fruit DROP COLUMN colorhex");
			stmt.executeUpdate("ALTER TABLE Fruit ADD COLUMN colorrgb VARCHAR(11)");
		}
	}

	@Override
	public void doAfter(StatelessSession session) throws Exception {
		my.db._2_0_0.classes.pkg.food.Fruit newFruit = new my.db._2_0_0.classes.pkg.food.Fruit();
		newFruit.name = data.name;
		newFruit.weight = data.weight;
		newFruit.colorRgb = hexToRgb(data.colorHex); // <--- complex operation that cannot be done in pure sql

		session.update(newFruit);
	}

	private static String hexToRgb(String hex) {
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		return r + "," + g + "," + b;
	}
}