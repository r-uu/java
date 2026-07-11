package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.core.AbstractDTO;
import de.ruu.lib.jpa.core.Entity;

class SimpleTypeDTO extends AbstractDTO<SimpleTypeEntity> implements SimpleType
{
	private final String name;

	SimpleTypeDTO(String name) { this.name = name; }

	// New helper constructor that takes over id/version
	SimpleTypeDTO(String name, Entity<Long> source)
	{
		this(name);
		// Access to protected method allowed within DTO class
		mapIdAndVersion(source);
	}

	@Override public String name() { return name; }

	public SimpleTypeEntity toSource()
	{
		SimpleTypeEntity entity = new SimpleTypeEntity(name);
		// Takes over id/version from DTO into entity
		entity.mapIdAndVersionFrom(this);
		return entity;
	}
}