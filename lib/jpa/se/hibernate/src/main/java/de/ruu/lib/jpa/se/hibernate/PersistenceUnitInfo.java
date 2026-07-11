package de.ruu.lib.jpa.se.hibernate;

import de.ruu.lib.jpa.core.AbstractPersistenceUnitInfo;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.sql.DataSource;
import java.util.List;

/** persistence unit info with hibernate as persistence provider */
public class PersistenceUnitInfo extends AbstractPersistenceUnitInfo
{
	public PersistenceUnitInfo(String persistenceUnitName, DataSource dataSource)
	{
		super(persistenceUnitName, HibernatePersistenceProvider.class, dataSource);
	}

	@Override public String getScopeAnnotationName() { return null; }

	@Override public List<String> getQualifierAnnotationNames() { return List.of(); }
}