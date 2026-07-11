package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.cdi.se.CDIContainer;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TransactionRequiredException;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
class TestAbstractRepository
{
	private static final Logger log = LoggerFactory.getLogger(TestAbstractRepository.class);

	/** bootstrap CDI in Java SE with {@code TransactionalInterceptorCDI} */
	@BeforeAll static void beforeAll()
	{
		CDIContainer.bootstrap(
				TestAbstractRepository.class.getClassLoader(),
				List.of(CDIExtension.class, EntityManagerProducer.class)); // explicitly register EntityManagerProducer
		log.debug("cdi bootstrapped successfully");
	}

	private SimpleTypeRepository repository;

	@BeforeEach void beforeEach()
	{
		repository = CDI.current().select(SimpleTypeRepository.class).get();
		assertThat(repository).isNotNull();
	}

	@Test void testAbstractRepositoryWithoutTransaction()
	{
		assertThatThrownBy(() ->
		{
			String name = "schmottekk";
			SimpleTypeEntity entity = new SimpleTypeEntity(name);
			entity = repository.create(entity);
			repository.entityManager().flush();
		}).isInstanceOf(TransactionRequiredException.class);
	}

	@Test void testAbstractRepositoryWithTransaction()
	{
		EntityTransaction transaction = repository.entityManager().getTransaction();

		transaction.begin();

		String name = "schmottekk";
		SimpleTypeEntity entity = new SimpleTypeEntity(name);
		entity = repository.create(entity);
		assertThat(entity       ).isNotNull();
		assertThat(entity.id()  ).isNotNull();
		assertThat(entity.name()).isEqualTo(name);

		name = "äffchen";
		entity.name(name);
		entity = repository.create(entity);
		assertThat(entity       ).isNotNull();
		assertThat(entity.id()  ).isNotNull();
		assertThat(entity.name()).isEqualTo(name);

		repository.delete(entity.getId());
		Optional<SimpleTypeEntity> optional = repository.find(entity.getId());
		assertThat(optional            ).isNotNull();
		assertThat(optional.isPresent()).isEqualTo(false);

		transaction.commit();
	}
}