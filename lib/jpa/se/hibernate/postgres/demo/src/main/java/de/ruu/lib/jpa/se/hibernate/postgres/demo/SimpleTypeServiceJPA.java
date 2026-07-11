package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.se.Transactional;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class SimpleTypeServiceJPA implements SimpleTypeService
{
	private static final Logger log = LoggerFactory.getLogger(SimpleTypeServiceJPA.class);

	@Inject private SimpleTypeRepository repository;

	@PostConstruct private void postConstruct() { log.debug("injected repository: {}", repository); }

	@Override public SimpleTypeEntity save(SimpleTypeEntity entity)
	{
		// If entity already has an ID, it's an update, otherwise create
		if (entity.id() != null)
		{
			return repository.update(entity);
		}
		else
		{
			return repository.create(entity);
		}
	}

	@Override public SimpleTypeEntity           update(SimpleTypeEntity entity) { return repository.update(entity); }
	@Override public Optional<SimpleTypeEntity> find  (Long             id)     { return repository.find  (id);     }
	@Override public void                       delete(Long             id)     {        repository.delete(id);     }
}