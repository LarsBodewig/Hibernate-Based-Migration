package my.db.migration.example;

import dev.bodewig.db2ascii.Db2Ascii;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateStatelessExample {

	public static void main(String[] args) throws SQLException, IllegalArgumentException, IllegalAccessException {
		try {
			// workaround for classloader mismatch due to different exec-maven-plugin
			// executions
			Class.forName("org.h2.Driver").getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Configuration configOld = new Configuration();
		configOld.configure("/db/V0_hibernate.cfg.xml");

		Configuration configNew = new Configuration();
		configNew.configure("/db/V1_hibernate.cfg.xml");

		try (Connection con = DriverManager.getConnection("jdbc:h2:mem:flyway");
				SessionFactory factoryOld = configOld.buildSessionFactory();
				SessionFactory factoryNew = configNew.buildSessionFactory();) {
			initialize(con, factoryOld);
			migrate(con, factoryOld, factoryNew);
		}
	}

	public static void initialize(Connection con, SessionFactory factoryOld)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
		try (Statement stmt = con.createStatement()) {
			stmt.executeUpdate("CREATE TABLE Fruit (name VARCHAR(255), weight INTEGER, colorhex CHAR(6))");
		}

		try (StatelessSession session = factoryOld.openStatelessSession(con)) {
			// insert data for 1.0.0
			Transaction transaction = session.beginTransaction();
			my.db._1_0_0.classes.pkg.food.Fruit oldFruit = new my.db._1_0_0.classes.pkg.food.Fruit();
			oldFruit.name = "Orange";
			oldFruit.weight = 250;
			oldFruit.colorHex = "ff8800";
			session.insert(oldFruit);
			transaction.commit();

			// print table contents
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<my.db._1_0_0.classes.pkg.food.Fruit> query = builder
					.createQuery(my.db._1_0_0.classes.pkg.food.Fruit.class);
			Root<my.db._1_0_0.classes.pkg.food.Fruit> root = query.from(my.db._1_0_0.classes.pkg.food.Fruit.class);
			query.select(root);
			Db2Ascii.printQueryResult(session.createQuery(query));
		}
	}

	public static void migrate(Connection con, SessionFactory factoryOld, SessionFactory factoryNew)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
		// read data from 1.0.0
		my.db._1_0_0.classes.pkg.food.Fruit oldFruit = null;
		try (StatelessSession session = factoryOld.openStatelessSession(con)) {
			oldFruit = session.get(my.db._1_0_0.classes.pkg.food.Fruit.class, "Orange");
		}

		// migrate scheme to 2.0.0
		try (Statement stmt = con.createStatement()) {
			stmt.executeUpdate("ALTER TABLE Fruit DROP COLUMN colorhex");
			stmt.executeUpdate("ALTER TABLE Fruit ADD COLUMN colorrgb VARCHAR(11)");
		}

		// migrate data to 2.0.0
		my.db._2_0_0.classes.pkg.food.Fruit newFruit = new my.db._2_0_0.classes.pkg.food.Fruit();
		newFruit.name = oldFruit.name;
		newFruit.weight = oldFruit.weight;
		newFruit.colorRgb = hexToRgb(oldFruit.colorHex); // <--- complex operation that cannot be done in pure sql

		try (StatelessSession session = factoryNew.openStatelessSession(con)) {
			// update data for 2.0.0
			Transaction transaction = session.beginTransaction();
			session.update(newFruit);
			transaction.commit();

			// print table contents
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<my.db._2_0_0.classes.pkg.food.Fruit> query = builder
					.createQuery(my.db._2_0_0.classes.pkg.food.Fruit.class);
			Root<my.db._2_0_0.classes.pkg.food.Fruit> root = query.from(my.db._2_0_0.classes.pkg.food.Fruit.class);
			query.select(root);
			Db2Ascii.printQueryResult(session.createQuery(query));
		}
	}

	private static String hexToRgb(String hex) {
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		return r + "," + g + "," + b;
	}
}
