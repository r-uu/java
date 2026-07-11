package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import de.ruu.lib.util.BooleanFunctions;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;

@Vetoed
@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
class TestSimpleTypeService
{
	private static final Logger log = LoggerFactory.getLogger(TestSimpleTypeService.class);

	private static SeContainer seContainer; // initialisation and closure handled in before/after all methods

	@SuppressWarnings("unchecked")
	@BeforeAll static void beforeAll()
	{
		log.debug("cdi container initialisation");
		try
		{
			// Because the beans.xml is located under src/main/resources the directory src/main/java is the bean archive.
			// Because CDIExtension is not located in the bean archive we need to add it explicitly. The other classes are
			// picked up automatically because of bean-discovery-mode="all" in beans.xml.
			seContainer =
					SeContainerInitializer
							.newInstance()
							.addExtensions     (CDIExtension               .class)
//							.addBeanClasses    (EntityManagerProducer      .class)
//							.addBeanClasses    (SimpleTypeService          .class)
//							.addBeanClasses    (SimpleTypeServiceJPA       .class)
//							.addBeanClasses    (SimpleTypeRepository       .class)
//							.addBeanClasses    (TransactionalInterceptorCDI.class)
							.enableInterceptors(TransactionalInterceptorCDI.class) // enable interceptors explicitely (on in beans.xml)
							.initialize();
		}
		catch (Exception e)
		{
			log.error("failure initialising seContainer", e);
		}
		log.debug("cdi container initialisation successful: {}", BooleanFunctions.not(isNull(seContainer)));
	}

	@AfterAll static void afterAll()
	{
		log.debug("cdi container shut down");
		if (seContainer != null) {
			seContainer.close();
			log.debug("cdi container shut down {}", seContainer.isRunning() ? "unsuccessful" : "successful");
		} else {
			log.warn("seContainer is null, nothing to shut down");
		}
	}

	@Test void testSimpleTypeService()
	{
		if (seContainer == null) {
			log.error("seContainer is null, test cannot run");
			return;
		}
		SimpleTypeService service = seContainer.select(SimpleTypeService.class).get();
		assertThat(service).isNotNull();

		String name = "schmottekk";
		SimpleTypeEntity entity = new SimpleTypeEntity(name);
		entity = service.save(entity);
		assertThat(entity).isNotNull();
		assertThat(entity.id()).isNotNull();
		assertThat(entity.name()).isEqualTo(name);

		name = "äffchen";
		entity.name(name);
		entity = service.save(entity);
		assertThat(entity).isNotNull();
		assertThat(entity.id()).isNotNull();
		assertThat(entity.name()).isEqualTo(name);

		service.delete(entity.getId());
		Optional<SimpleTypeEntity> optional = service.find(entity.getId());
		assertThat(optional).isNotNull();
		assertThat(optional.isPresent()).isEqualTo(false);
	}
}