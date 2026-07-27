package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.core.AbstractRepository;
import de.ruu.lib.jpa.se.hibernate.postgres.demo.HibernatePostgresDemoQualifier;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Singleton
class SimpleTypeRepository extends AbstractRepository<SimpleTypeEntity, Long>
{
	private static final Logger log = LogManager.getLogger(SimpleTypeRepository.class);

	@HibernatePostgresDemoQualifier
	@Inject private EntityManager entityManager;

	@PostConstruct private void postConstruct() { log.debug("injected entity manager successfully: {}", entityManager != null); }

	@Override protected EntityManager entityManager() { return entityManager; }
}