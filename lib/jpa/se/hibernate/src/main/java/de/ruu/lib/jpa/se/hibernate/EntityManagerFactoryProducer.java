package de.ruu.lib.jpa.se.hibernate;

import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_PASSWORD;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_USER;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.jpa.HibernatePersistenceProvider;

/**
 * {@link #produce(String, String)} method returns an {@link EntityManagerFactory} that is configured with the given
 * {@link PersistenceUnitInfo} and {@link PersistenceUnitProperties}. The returned {@link EntityManagerFactory} is
 * prepared to produce instances of {@link jakarta.persistence.EntityManager} based on hibernate.
 */
public class EntityManagerFactoryProducer
{
	private final PersistenceUnitInfo       persistenceUnitInfo;
	private final PersistenceUnitProperties persistenceUnitProperties;

	public EntityManagerFactoryProducer(
			PersistenceUnitInfo       persistenceUnitInfo,
			PersistenceUnitProperties persistenceUnitProperties)
	{
		this.persistenceUnitInfo       = persistenceUnitInfo;
		this.persistenceUnitProperties = persistenceUnitProperties;
	}

	public EntityManagerFactory produce(String username, String password)
	{
		Map<String, Object> properties = new HashMap<>(persistenceUnitProperties.properties());

		properties.put(JAKARTA_JDBC_USER,     username);
		properties.put(JAKARTA_JDBC_PASSWORD, password);

		return
				new HibernatePersistenceProvider()
						.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
	}
}
