package my.db.migration.example;

import dev.bodewig.db2ascii.Db2Ascii;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;

public class HibernateMigrationExample {

	public static void main(String[] args) throws SQLException, IllegalArgumentException, IllegalAccessException {
		try {
			// workaround for classloader mismatch due to different exec-maven-plugin
			// executions
			Class.forName("org.h2.Driver").getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try (Connection con = DriverManager.getConnection("jdbc:h2:mem:hibernatemigrate;DB_CLOSE_ON_EXIT=FALSE")) {
			initialize(con);

			Flyway flyway = Flyway.configure().dataSource("jdbc:h2:mem:hibernatemigrate;DB_CLOSE_ON_EXIT=FALSE", "", "")
					.locations("classpath:my/db/migration/example").baselineVersion("0").load();
			flyway.baseline();
			flyway.migrate();

			try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Fruit")) {
				Db2Ascii.printResultSet(rs);
			}
		}
	}

	public static void initialize(Connection con)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
		try (Statement stmt = con.createStatement()) {
			stmt.executeUpdate("CREATE TABLE Fruit (name VARCHAR(255), weight INTEGER, colorhex CHAR(6))");

			// insert data for 1.0.0
			my.db._1_0_0.classes.pkg.food.Fruit oldFruit = new my.db._1_0_0.classes.pkg.food.Fruit();
			oldFruit.name = "Orange";
			oldFruit.weight = 250;
			oldFruit.colorHex = "ff8800";
			PreparedStatement ps = con.prepareStatement("INSERT INTO Fruit (name, weight, colorhex) VALUES (?, ?, ?)");
			ps.setString(1, oldFruit.name);
			ps.setInt(2, oldFruit.weight);
			ps.setString(3, oldFruit.colorHex);
			ps.execute();

			// print table contents
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM Fruit")) {
				Db2Ascii.printResultSet(rs);
			}
		}
	}
}
