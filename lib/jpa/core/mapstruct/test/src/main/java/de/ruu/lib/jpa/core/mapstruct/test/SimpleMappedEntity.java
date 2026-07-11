package de.ruu.lib.jpa.core.mapstruct.test;

import de.ruu.lib.jpa.core.mapstruct.AbstractMappedEntity;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

import static de.ruu.lib.util.Strings.isEmptyOrBlank;

class SimpleMappedEntity extends AbstractMappedEntity<SimpleMappedDTO>
{
	@NonNull private String name;

	SimpleMappedEntity(@NonNull String name)
	{
		this.name = Objects.requireNonNull(name, "name");
	}

	public String name() { return name; }

	public void name(@NonNull String v) { this.name = Objects.requireNonNull(v, "name"); }

	// Override as public to allow MapStruct generated code access from different package
	@Override
	public void beforeMapping(de.ruu.lib.jpa.core.Entity<Long> input) {
		super.beforeMapping(input);
	}

	@Override public @NonNull SimpleMappedDTO toTarget() { return Mapper.INSTANCE.map(this); }

	void setName(@NonNull String name)
	{
		if (isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be null nor empty");
		this.name = name;
	}
}
