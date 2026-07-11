package de.ruu.lib.jpa.core.mapstruct.demo.bidirectional;

import de.ruu.lib.jpa.core.mapstruct.AbstractMappedEntity;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class EmployeeEntity extends AbstractMappedEntity<EmployeeDTO>
{
	private static final Logger log = LoggerFactory.getLogger(EmployeeEntity.class);

	/** mutable, but not nullable */
	@NonNull private String name;

	/**
	 * mutable, but not nullable
	 * <p>no java-bean-style accessor here, mapstruct will ignore fields without bean-style-accessor so mapping can be
	 * controlled in beforeMapping
	 */
	@NonNull private DepartmentEntity department;

	protected EmployeeEntity() { }

	/** provide handmade required args constructor to properly handle relationships */
	EmployeeEntity(@NonNull DepartmentEntity department, @NonNull String name)
	{
		this.department = department;
		name(name);
		department.add(this);
	}

	public @NonNull String           name()       { return name; }
	public @NonNull DepartmentEntity department() { return department; }

	public void name      (@NonNull String           v) { this.name       = Objects.requireNonNull(v); }
	public void department(@NonNull DepartmentEntity v) { this.department = Objects.requireNonNull(v); }

	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	public @NonNull String getName() { return name(); }

	void beforeMapping(@NonNull EmployeeDTO target)
	{
		super.beforeMapping(target);
		log.debug("before mapping starting");
		log.debug("before mapping finished");
	}

	void afterMapping(@NonNull EmployeeDTO target)
	{
		super.afterMapping(target);
		log.debug("after mapping starting");
		log.debug("after mapping finished");
	}

	@Override public @NonNull EmployeeDTO toTarget() { return Mapper.INSTANCE.map(this); }

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof EmployeeEntity other)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(name, other.name);
	}

	@Override public int hashCode() { return Objects.hash(super.hashCode(), name); }

	@Override public String toString()
	{
		return super.toString() + ", EmployeeEntity(name=" + name + ")";
	}
}
