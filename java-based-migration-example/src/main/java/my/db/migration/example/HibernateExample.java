package my.db.migration.example;

import java.sql.SQLException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateExample {

	public static void main(String[] args) throws SQLException {
		Configuration configOld = new Configuration();
		configOld.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
		configOld.setProperty("hibernate.connection.url", "jdbc:h2:mem:hibernate");
		configOld.setProperty("hibernate.connection.username", "sa");
		configOld.setProperty("hibernate.connection.password", "");
		configOld.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		configOld.addAnnotatedClass(my.db._1_0_0.classes.pkg.food.Fruit.class);

		Configuration configNew = new Configuration();
		configNew.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
		configNew.setProperty("hibernate.connection.url", "jdbc:h2:mem:hibernate");
		configNew.setProperty("hibernate.connection.username", "sa");
		configNew.setProperty("hibernate.connection.password", "");
		configNew.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		configNew.addAnnotatedClass(my.db._2_0_0.classes.pkg.food.Fruit.class);

		try (SessionFactory factoryOld = configOld.buildSessionFactory();
				SessionFactory factoryNew = configNew.buildSessionFactory()) {
			initialize(factoryOld);
			migrate(factoryOld, factoryNew);
		}
	}

	public static void initialize(SessionFactory factoryOld) throws SQLException {
		try (Session session = factoryOld.openSession()) {
			session.doWork(con -> {
				con.createStatement()
						.executeUpdate("CREATE TABLE Fruit (name VARCHAR(255), weight INTEGER, colorhex CHAR(6))");
			});

			// insert data for 1.0.0
			Transaction transaction = session.beginTransaction();
			my.db._1_0_0.classes.pkg.food.Fruit oldFruit = new my.db._1_0_0.classes.pkg.food.Fruit();
			oldFruit.name = "Orange";
			oldFruit.weight = 250;
			oldFruit.colorHex = "ff8800";
			session.persist(oldFruit);
			transaction.commit();

			// print table contents
			my.db._1_0_0.classes.pkg.food.Fruit oldFruitFromDb = session.find(my.db._1_0_0.classes.pkg.food.Fruit.class,
					"Orange");
			System.out.println("TABLE Fruit initialized:");
			System.out.println("|  name  | weight | color_hex |");
			System.out.println("|--------|--------|-----------|");
			System.out.println("| %s |   %d  |  #%s  |".formatted(oldFruitFromDb.name, oldFruitFromDb.weight,
					oldFruitFromDb.colorHex));
		}
	}

	public static void migrate(SessionFactory factoryOld, SessionFactory factoryNew) throws SQLException {
		// read data from 1.0.0
		my.db._1_0_0.classes.pkg.food.Fruit oldFruit = null;
		try (Session session = factoryOld.openSession()) {
			oldFruit = session.find(my.db._1_0_0.classes.pkg.food.Fruit.class, "Orange");
		}

		// migrate data to 2.0.0
		my.db._2_0_0.classes.pkg.food.Fruit newFruit = new my.db._2_0_0.classes.pkg.food.Fruit();
		newFruit.name = oldFruit.name;
		newFruit.weight = oldFruit.weight;
		newFruit.colorRgb = hexToRgb(oldFruit.colorHex); // <--- complex operation that cannot be done in pure sql

		try (Session session = factoryNew.openSession()) {
			// migrate scheme to 2.0.0
			session.doWork(con -> {
				con.createStatement().executeUpdate("ALTER TABLE Fruit DROP COLUMN colorhex");
				con.createStatement().executeUpdate("ALTER TABLE Fruit ADD COLUMN colorrgb VARCHAR(11)");
			});

			// update data for 2.0.0
			Transaction transaction = session.beginTransaction();
			session.persist(newFruit);
			transaction.commit();

			// print table contents
			my.db._2_0_0.classes.pkg.food.Fruit newFruitFromDb = session.find(my.db._2_0_0.classes.pkg.food.Fruit.class,
					"Orange");
			System.out.println("TABLE Fruit migrated:");
			System.out.println("|  name  | weight | color_rgb |");
			System.out.println("|--------|--------|-----------|");
			System.out.println("| %s |   %d  | %s |".formatted(newFruitFromDb.name, newFruitFromDb.weight,
					newFruitFromDb.colorRgb));
		}
	}

	private static String hexToRgb(String hex) {
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		return r + "," + g + "," + b;
	}
}
