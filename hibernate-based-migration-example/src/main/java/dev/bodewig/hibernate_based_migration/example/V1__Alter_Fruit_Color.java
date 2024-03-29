package dev.bodewig.hibernate_based_migration.example;

import dev.bodewig.hibernate_based_migration.HibernateMigration;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.flywaydb.core.api.migration.Context;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

public class V1__Alter_Fruit_Color extends HibernateMigration {

	private static final long serialVersionUID = 2198717374000299523L;

	private List<dev.bodewig.hibernate_based_migration._1.example.Fruit> fruits;

	@Override
	protected long getSerialVersionUID() {
		return serialVersionUID;
	}

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
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<dev.bodewig.hibernate_based_migration._1.example.Fruit> query = builder
				.createQuery(dev.bodewig.hibernate_based_migration._1.example.Fruit.class);
		Root<dev.bodewig.hibernate_based_migration._1.example.Fruit> root = query
				.from(dev.bodewig.hibernate_based_migration._1.example.Fruit.class);
		query.select(root);
		this.fruits = session.createQuery(query).getResultList();
	}

	@Override
	public void doSql(Context context) throws SQLException {
		try (Statement stmt = context.getConnection().createStatement()) {
			stmt.executeUpdate("ALTER TABLE Fruit DROP COLUMN colorHex");
			stmt.executeUpdate("ALTER TABLE Fruit ADD COLUMN colorRgb VARCHAR(11)");
		}
	}

	@Override
	public void doAfter(StatelessSession session) {
		this.fruits.stream().map(fruitBefore -> {
			dev.bodewig.hibernate_based_migration._2.example.Fruit fruitAfter = new dev.bodewig.hibernate_based_migration._2.example.Fruit();
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
