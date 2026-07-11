package de.ruu.lib.jpa.se;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for transactional interceptors. Extend this class to provide a suitable {@link #entityManager()}
 * implementation.
 *
 * IMPORTANT
 * <br>
 * DO NOT FORGET TO MENTION THIS <code>@Interceptor</code> TYPE IN <code>beans.xml</code>, e. g.:
 * <pre>
 * <beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
 *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *        xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
 *        bean-discovery-mode="all">
 *
 * 	<interceptors>
 * 		<class>de.ruu.lib.jpa.se.hibernate.postgres.demo.EntityManagerProducer.TransactionalInterceptorCDI</class>
 * 	</interceptors>
 *
 * </beans>
 * </pre>
 * OR TO INITIALISE SeContainer LIKE THIS:
 * <pre>
 * {@code
 * SeContainer container =
 *     SeContainerInitializer
 *         .newInstance()
 *         .addBeanClasses    (<ExtensionOf>TransactionalInterceptorCDI.class)
 *         .enableInterceptors(<ExtensionOf>TransactionalInterceptorCDI.class)
 *         .selectAlternatives(MockGenerator.class)
 *         .initialize();
 * }
 * </pre>
 *
 * @author r-uu
 */
public abstract class AbstractTransactionalInterceptor
{
	private static final Logger log = LoggerFactory.getLogger(AbstractTransactionalInterceptor.class);

	protected abstract EntityManager entityManager();

	@AroundInvoke
	public Object transaction(InvocationContext context) throws Exception
	{
		Object            result;
		boolean           startedTransaction = false;
		EntityTransaction transaction        = entityManager().getTransaction();
		String            methodName         =
				context.getMethod().getDeclaringClass().getName() + "." + context.getMethod().getName();

		log.trace("\nentity manager {}, transaction {}", entityManager().hashCode(), transaction.hashCode());

		try
		{
			if (transaction.isActive())
			{
				log.debug("resuming active transaction {}", transaction);
			}
			else
			{
				log.debug("starting transaction {}", transaction);
				transaction.begin();
				startedTransaction = true;
			}

			log.trace("calling           {} in transaction {}", methodName, transaction);
			result = context.proceed();
			log.trace("returned from     {} in transaction {}", methodName, transaction);
		}
		catch (Throwable t)
		{
			log.error("failure executing {} in transaction {}", methodName, transaction, t);
			throw t;
		}
		finally
		{
			if (startedTransaction)
			{
				if (transaction.isActive())
				{
					try
					{
						log.debug("committing transaction {}", transaction);
						transaction.commit();
						log.debug("commit succeeded, transaction {}", transaction);
					}
					catch (RuntimeException e)
					{
						log.debug("rolling back transaction {}", transaction);
						transaction.rollback();
						log.debug("rollback succeeded, transaction {}", transaction);
					}
				}
				else
				{
					log.warn("can't commit inactive transaction {}", transaction);
				}
			}
			else
			{
				log.trace("resuming transaction {}", transaction);
			}
		}

		return result;
	}
}