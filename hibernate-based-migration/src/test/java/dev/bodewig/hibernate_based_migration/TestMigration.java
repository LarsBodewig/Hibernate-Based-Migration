package dev.bodewig.hibernate_based_migration;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.flywaydb.core.api.migration.Context;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

public class TestMigration extends HibernateMigration {

	private List<FruitBefore> fruits;

	@Override
	public Configuration configBefore() {
		return new Configuration().configure("/db/V0_hibernate.cfg.xml");
	}

	@Override
	public Configuration configAfter() {
		return new Configuration().configure("/db/V1_hibernate.cfg.xml");
	}

	@Override
	public void doBefore(StatelessSession session) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<FruitBefore> query = builder.createQuery(FruitBefore.class);
		Root<FruitBefore> root = query.from(FruitBefore.class);
		query.select(root);
		this.fruits = session.createQuery(query).getResultList();
	}

	@Override
	public void doSql(Context context) throws SQLException {
		try (Statement stmt = context.getConnection().createStatement()) {
			stmt.executeUpdate("ALTER TABLE Fruit DROP COLUMN color_hex");
			stmt.executeUpdate("ALTER TABLE Fruit ADD COLUMN color_rgb VARCHAR(11)");
		}
	}

	@Override
	public void doAfter(StatelessSession session) {
		this.fruits.stream().map(fruitBefore -> {
			FruitAfter fruitAfter = new FruitAfter();
			fruitAfter.name = fruitBefore.name;
			fruitAfter.weight = fruitBefore.weight;
			fruitAfter.colorRgb = hexToRgb(fruitBefore.colorHex);
			return fruitAfter;
		}).forEach(fruitAfter -> session.update(fruitAfter));
	}

	private static String hexToRgb(String hex) {
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		return r + "," + g + "," + b;
	}
}
