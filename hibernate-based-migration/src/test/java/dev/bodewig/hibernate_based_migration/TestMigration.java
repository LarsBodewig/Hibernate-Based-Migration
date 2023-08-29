package dev.bodewig.hibernate_based_migration;

@Migration(from = "1.0.0", fromCfg = "/V1__hibernate.cfg.xml", to = "2.0.0", toCfg = "/V2__hibernate.cfg.xml")
public class TestMigration extends HibernateMigration {

}
