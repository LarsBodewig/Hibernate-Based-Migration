package dev.bodewig.hibernate_based_migration;

import java.io.Serializable;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.extensibility.MigrationType;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

/**
 * A abstract class to implement Hibernate Migrations - an extension of Flyway's
 * Java based Migrations allowing for complex migration operations on persisted
 * data.
 * <p>
 * <b>Hibernate Migrations should always operate on frozen classes and
 * resources.</b>
 * <p>
 * Subclasses should maintain and return a {@code serialVersionUID} to provide a
 * checksum.
 * <p>
 * Your class should be named according to the rules by
 * {@link BaseJavaMigration}:
 * <ul>
 * <li><strong>Prefix:</strong> V for versioned migrations, U for undo
 * migrations, R for repeatable migrations</li>
 * <li><strong>Version:</strong> Underscores (automatically replaced by dots at
 * runtime) separate as many parts as you like (Not for repeatable
 * migrations)</li>
 * <li><strong>Separator:</strong> __ (two underscores)</li>
 * <li><strong>Description:</strong> Underscores (automatically replaced by
 * spaces at runtime) separate the words</li>
 * </ul>
 */
public abstract class HibernateMigration extends BaseJavaMigration implements Serializable {

	/**
	 * Called by Flyway to create the migration.
	 */
	public HibernateMigration() {
	}

	/**
	 * Only relevant in subclasses
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The Hibernate configration used in {@link #doBefore}
	 */
	protected Configuration configBefore;

	/**
	 * The Hibernate configration used in {@link #doAfter}
	 */
	protected Configuration configAfter;

	/**
	 * Implements versioning based on {@link BaseJavaMigration} and loads the
	 * Hibernate configurations
	 */
	@Override
	protected void init() {
		super.init();
		configBefore = configBefore();
		configAfter = configAfter();
	}

	/**
	 * {@link CoreMigrationType#JDBC} by default, can be overriden with custom type
	 */
	@Override
	public MigrationType getType() {
		return CoreMigrationType.JDBC;
	}

	/**
	 * <b>Should usually not be overriden.</b>
	 * <p>
	 * Creates {@code SessionFactor}ies from both configurations and calls
	 * {@link #doBefore}, {@link #doSql} and {@link #doAfter} in this order.
	 */
	@Override
	public void migrate(Context context) throws Exception {
		try (SessionFactory factoryBefore = configBefore.buildSessionFactory();
				SessionFactory factoryAfter = configAfter.buildSessionFactory()) {
			try (StatelessSession sessionBefore = factoryBefore.openStatelessSession(context.getConnection())) {
				doBefore(sessionBefore);
			}
			doSql(context);
			try (StatelessSession sessionAfter = factoryAfter.openStatelessSession(context.getConnection())) {
				doAfter(sessionAfter);
			}
		}
	}

	/**
	 * Implements checksum mechanism using the {@code serialVersionUID}
	 */
	@Override
	public Integer getChecksum() {
		return Long.hashCode(getSerialVersionUID());
	}

	/**
	 * Return the {@code serialVersionUID}. Used to implement the checksum
	 * mechanism.
	 * 
	 * @return the serialVersionUID
	 * @see #getChecksum
	 */
	protected abstract long getSerialVersionUID();

	/**
	 * Load or create a Hibernate configuration used in {@link #doBefore}
	 * 
	 * @return the Hibernate configuration
	 */
	public abstract Configuration configBefore();

	/**
	 * Load or create a Hibernate configuration used in {@link #doAfter}
	 * 
	 * @return the Hibernate configuration
	 */
	public abstract Configuration configAfter();

	/**
	 * Called before applying {@link #doSql}. Intended to load data using the
	 * {@link #configBefore} and carry that data over in the class scope.
	 * 
	 * @param session
	 *            a StatelessSession created from the {@link #configBefore}
	 * @throws Exception
	 *             any Exception
	 */
	public abstract void doBefore(StatelessSession session) throws Exception;

	/**
	 * Do the actual SQL migration. This method executes the database scheme changes
	 * similar to a Flyway SQL script.
	 * 
	 * @param context
	 *            a Flyway context
	 * @throws Exception
	 *             any Exception
	 */
	public abstract void doSql(Context context) throws Exception;

	/**
	 * Called after applying {@link #doSql}. Intended to write data using the
	 * {@link #configAfter}.
	 * 
	 * @param session
	 *            a StatelessSession created from the {@link #configAfter}
	 * @throws Exception
	 *             any Exception
	 */
	public abstract void doAfter(StatelessSession session) throws Exception;
}
