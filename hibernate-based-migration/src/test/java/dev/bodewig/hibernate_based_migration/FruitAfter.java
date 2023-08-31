package dev.bodewig.hibernate_based_migration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FruitAfter {
	@Id
	public String name;
	public int weight;
	public String colorRgb;
}