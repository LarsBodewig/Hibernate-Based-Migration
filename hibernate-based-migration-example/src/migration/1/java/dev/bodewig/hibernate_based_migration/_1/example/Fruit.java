package dev.bodewig.hibernate_based_migration._1.example;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class Fruit {
    @Id
    public String name;

    public int weight;

    public String colorHex;
}