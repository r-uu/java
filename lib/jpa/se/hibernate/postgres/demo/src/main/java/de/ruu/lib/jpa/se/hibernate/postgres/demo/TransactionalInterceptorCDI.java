package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.se.AbstractTransactionalInterceptor;
import de.ruu.lib.jpa.se.Transactional;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import jakarta.persistence.EntityManager;

@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Transactional
public class TransactionalInterceptorCDI extends AbstractTransactionalInterceptor
{
	@HibernatePostgresDemoQualifier
	@Inject
	private EntityManager entityManager;

	@Override
	protected EntityManager entityManager()
	{
		return entityManager;
	}
}