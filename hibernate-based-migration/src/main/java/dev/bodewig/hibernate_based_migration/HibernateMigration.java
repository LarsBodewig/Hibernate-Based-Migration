package dev.bodewig.hibernate_based_migration;

import java.io.Serializable;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.extensibility.MigrationType;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

@SuppressWarnings("serial")
public abstract class HibernateMigration extends BaseJavaMigration implements Serializable {

	protected Configuration configBefore;

	protected Configuration configAfter;

	@Override
	protected void init() {
		super.init();
		configBefore = configBefore();
		configAfter = configAfter();
	}

	@Override
	public MigrationType getType() {
		return CoreMigrationType.JDBC;
	}

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

	@Override
	public Integer getChecksum() {
		return Long.hashCode(getSerialVersionUID());
	}

	protected abstract long getSerialVersionUID();

	public abstract Configuration configBefore();

	public abstract Configuration configAfter();

	public abstract void doBefore(StatelessSession session) throws Exception;

	public abstract void doSql(Context context) throws Exception;

	public abstract void doAfter(StatelessSession session) throws Exception;
}
