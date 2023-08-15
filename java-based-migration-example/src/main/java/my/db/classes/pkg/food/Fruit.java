package my.db.classes.pkg.food;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Fruit {
	@Id
	public String name;
	public int weight;
	public String colorRgb;
}
