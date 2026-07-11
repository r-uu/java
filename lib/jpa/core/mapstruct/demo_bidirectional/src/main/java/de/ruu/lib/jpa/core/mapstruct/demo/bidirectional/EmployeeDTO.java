package de.ruu.lib.jpa.core.mapstruct.demo.bidirectional;

import de.ruu.lib.jpa.core.mapstruct.AbstractMappedDTO;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class EmployeeDTO extends AbstractMappedDTO<EmployeeEntity>
{
	private static final Logger log = LoggerFactory.getLogger(EmployeeDTO.class);

	/** mutable non-null */
	@NonNull private String name;

	/** mutable non-null */
	@NonNull private DepartmentDTO department;

	protected EmployeeDTO() { }

	/** provide handmade required args constructor to properly handle relationships */
	EmployeeDTO(@NonNull DepartmentDTO department, @NonNull String name)
	{
		Objects.requireNonNull(name, "name");
		this.department = department;
		name(name);
		department.add(this);
	}

	public @NonNull String        name()       { return name; }
	public @NonNull DepartmentDTO department() { return department; }

	public void name      (@NonNull String        v) { this.name       = v; }
	public void department(@NonNull DepartmentDTO v) { this.department = v; }

	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	public @NonNull String getName() { return name(); }

	void beforeMapping(@NonNull EmployeeEntity source)
	{
		super.beforeMapping(source);
		log.debug("before mapping starting");
		log.debug("before mapping finished");
	}

	void afterMapping(@NonNull EmployeeEntity source)
	{
		super.afterMapping(source);
		log.debug("after mapping starting");
		log.debug("after mapping finished");
	}

	@Override public @NonNull EmployeeEntity toSource() { return Mapper.INSTANCE.map(this); }

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof EmployeeDTO other)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(name, other.name);
	}

	@Override public int hashCode() { return Objects.hash(super.hashCode(), name); }

	@Override public String toString()
	{
		return super.toString() + ", EmployeeDTO(name=" + name + ")";
	}
}
