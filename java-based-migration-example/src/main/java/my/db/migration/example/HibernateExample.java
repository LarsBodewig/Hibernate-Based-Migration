package my.db.migration.example;

import dev.bodewig.db2ascii.Db2Ascii;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.sql.SQLException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateExample {

	public static void main(String[] args) throws SQLException, IllegalArgumentException, IllegalAccessException {
		Configuration configOld = new Configuration();
		configOld.configure("/db/V0_hibernate.cfg.xml");

		Configuration configNew = new Configuration();
		configNew.configure("/db/V1_hibernate.cfg.xml");

		try (SessionFactory factoryOld = configOld.buildSessionFactory();
				SessionFactory factoryNew = configNew.buildSessionFactory()) {
			initialize(factoryOld);
			migrate(factoryOld, factoryNew);
		}
	}

	public static void initialize(SessionFactory factoryOld)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
		try (Session session = factoryOld.openSession()) {
			// insert data for 1.0.0
			Transaction transaction = session.beginTransaction();
			my.db._1_0_0.classes.pkg.food.Fruit oldFruit = new my.db._1_0_0.classes.pkg.food.Fruit();
			oldFruit.name = "Orange";
			oldFruit.weight = 250;
			oldFruit.colorHex = "ff8800";
			session.persist(oldFruit);
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

	public static void migrate(SessionFactory factoryOld, SessionFactory factoryNew)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
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
			// update data for 2.0.0
			Transaction transaction = session.beginTransaction();
			session.merge(newFruit);
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
