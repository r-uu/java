package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.se.AbstractTransactionalInterceptor;
import de.ruu.lib.jpa.se.Transactional;
import de.ruu.lib.jpa.se.hibernate.postgres.AbstractEntityManagerProducer;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.interceptor.Interceptor;
import jakarta.persistence.EntityManager;
import java.util.List;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

@Singleton public class EntityManagerProducer extends AbstractEntityManagerProducer
{
	@Override public List<Class<?>> managedClasses() { return List.of(SimpleTypeEntity.class); }

	/**
	 * The {@link Produces} annotation makes CDI call this method when an {@link EntityManager} needs to be injected.
	 * @return entity manager created by {@link AbstractEntityManagerProducer}
	 */
	@HibernatePostgresDemoQualifier
	@Produces
	@Override public EntityManager produce() { return super.produce(); }

	/** define an own interceptor for transactions */
	@Interceptor
	@Priority(APPLICATION) // this makes the interceptor available in other modules / archives
	@Transactional
	public static class TransactionalInterceptorCDI extends AbstractTransactionalInterceptor
	{
		@HibernatePostgresDemoQualifier
		@Inject private EntityManager entityManager;
		@Override protected EntityManager entityManager() { return entityManager; }
	}
}