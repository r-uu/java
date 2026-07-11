package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.core.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "simple_type")  // Verwendet standard "public" schema
public class SimpleTypeEntity extends AbstractEntity<SimpleTypeDTO> implements SimpleType
{
	private String name;

	protected SimpleTypeEntity() { name = ""; } // required by JPA

	SimpleTypeEntity(String name) { this.name = name; }

	public String name()            { return name; }
	public void   name(String name) { this.name = name; }

	SimpleTypeDTO toDTO()
	{
		// Uses the DTO constructor that internally takes over id/version
		return new SimpleTypeDTO(name, this);
	}

	// Package-private wrapper to take over id/version from a DTO
	void mapIdAndVersionFrom(SimpleTypeDTO dto) { mapIdAndVersion(dto); }

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof SimpleTypeEntity other)) return false;
		return super.equals(o) && Objects.equals(name, other.name);
	}

	@Override public int hashCode() { return Objects.hash(super.hashCode(), name); }
}
