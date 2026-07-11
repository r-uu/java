package de.ruu.lib.jpa.core.mapstruct.demo.bidirectional;

import de.ruu.lib.jpa.core.mapstruct.AbstractMappedEntity;
import de.ruu.lib.util.Strings;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;

public class DepartmentEntity extends AbstractMappedEntity<DepartmentDTO>
{
	private static final Logger log = LoggerFactory.getLogger(DepartmentEntity.class);

	/** mutable non-null */
	@NonNull private String name;

	/** mutable */
	private String description;

	/** no direct access to nullable modifiable set */
	private Set<EmployeeEntity> employees;

	protected DepartmentEntity() { }

	/* do not use lombok to make sure that fluent setter with its validation is called */
	public DepartmentEntity(@NonNull String name)
	{
		this();     // just in case something important happens in the default constructor
		name(name); // use fluent setter with its validation
	}

	public String name()        { return name; }
	public String description() { return description; }

	public void description(String v) { this.description = v; }

	public @NonNull DepartmentEntity name(@NonNull String name)
	{
		if (Strings.isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	void beforeMapping(@NonNull DepartmentDTO input)
	{
		super.beforeMapping(input);
		if (input.optionalEmployees().isPresent())
				input.optionalEmployees().get().forEach(e -> add(e.toSource()));
		name(input.name());
	}

	void afterMapping(@NonNull DepartmentDTO input)
	{
		super.afterMapping(input);
		log.debug("starting");
		log.debug("finished");
	}

	@Override public @NonNull DepartmentDTO toTarget() { return Mapper.INSTANCE.map(this); }

	/** return optional unmodifiable */
	public Optional<Set<EmployeeEntity>> optionalEmployees()
	{
		if (isNull(employees)) return Optional.empty();
		return Optional.of(Set.copyOf(employees));
	}

	public boolean add(@NonNull EmployeeEntity employee)
	{
		if (employee.department() == this)
		{
			if (employeesContains(employee)) return true;
			return nonNullEmployees().add(employee);
		}
		else
		{
			// following check should never return true
			if (employeesContains(employee))
					log.error("employee with {} is already contained in {}", employee.department(), this);

			// assign this department as department of employee and update employees
			employee.department(this);
			return nonNullEmployees().add(employee);
		}
	}

	public boolean remove(@NonNull EmployeeEntity employee)
	{
		if (isNull(employees)) return false;
		return employees.remove(employee);
	}

	private Set<EmployeeEntity> nonNullEmployees()
	{
		if (isNull(employees)) employees = new HashSet<>();
		return employees;
	}

	private boolean employeesContains(EmployeeEntity employee)
	{
		if (isNull(employees)) return false;
		return employees.contains(employee);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof DepartmentEntity other)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(name, other.name) && Objects.equals(description, other.description);
	}

	@Override public int hashCode() { return Objects.hash(super.hashCode(), name, description); }

	@Override public String toString()
	{
		return super.toString() + ", DepartmentEntity(name=" + name + ", description=" + description + ")";
	}
}
