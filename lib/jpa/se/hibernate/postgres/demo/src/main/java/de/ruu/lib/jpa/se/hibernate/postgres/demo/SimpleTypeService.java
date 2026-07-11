package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import java.util.Optional;

public interface SimpleTypeService
{
	SimpleTypeEntity           save(  SimpleTypeEntity entity);
	SimpleTypeEntity           update(SimpleTypeEntity entity);
	Optional<SimpleTypeEntity> find(  Long             id);
	void                       delete(Long             id);
}