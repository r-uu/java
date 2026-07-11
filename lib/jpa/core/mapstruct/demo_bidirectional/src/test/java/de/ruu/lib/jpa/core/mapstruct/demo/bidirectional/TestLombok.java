package de.ruu.lib.jpa.core.mapstruct.demo.bidirectional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestLombok
{
	@Test void emptyDepartmentDTO()
	{
		DepartmentDTO department = new DepartmentDTO();

		assertThat(department.id()         ).isNull();
		assertThat(department.name()       ).isNull();
		assertThat(department.description()).isNull();
	}

	@Test void emptyEmployeeDTO()
	{
		EmployeeDTO employee = new EmployeeDTO();

		assertThat(employee.id()        ).isNull();
		assertThat(employee.name()      ).isNull();
		assertThat(employee.department()).isNull();
	}

	@Test void emptyDepartmentEntity()
	{
		DepartmentEntity department = new DepartmentEntity();

		assertThat(department.id()         ).isNull();
		assertThat(department.name()       ).isNull();
		assertThat(department.description()).isNull();
	}

	@Test void emptyEmployeeEntity()
	{
		EmployeeEntity employee = new EmployeeEntity();

		assertThat(employee.id()        ).isNull();
		assertThat(employee.name()      ).isNull();
		assertThat(employee.department()).isNull();
	}

	@Test void invalidNameDepartmentDTO()
	{
		String name = null;

		assertThatThrownBy(() -> new DepartmentDTO(name))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void invalidNameEmployeeDTO()
	{
		String name = null;
		DepartmentDTO department = new DepartmentDTO("name");

		assertThatThrownBy(() -> new EmployeeDTO(department, name))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void invalidDepartmentEmployeeDTO()
	{
		String name = "name";
		DepartmentDTO department = null;

		assertThatThrownBy(() -> new EmployeeDTO(department, name))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void invalidNameDepartmentEntity()
	{
		String name = null;

		assertThatThrownBy(() -> new DepartmentEntity(name))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void invalidNameEmployeeEntity()
	{
		String name = null;
		DepartmentEntity department = new DepartmentEntity("name");

		assertThatThrownBy(() -> new EmployeeEntity(department, name))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void validDepartmentDTO()
	{
		String        name           = "name";
		DepartmentDTO departmentDTO  = new DepartmentDTO(name);

		assertThat(departmentDTO.name()       ).isEqualTo(name);
		assertThat(departmentDTO.description()).isNull();
	}

	@Test void validEmployeeInDepartmentDTO()
	{
		String        name       = "name";
		DepartmentDTO department = new DepartmentDTO(name);
		EmployeeDTO   employee   = new EmployeeDTO(department, name);

		assertThat(employee.name()      ).isEqualTo(name);
		assertThat(employee.department()).isEqualTo(department);
	}

	@Test void invalidEmptyDepartmentNameSetting()
	{
		DepartmentEntity department = new DepartmentEntity();
		assertThatThrownBy(() -> department.name(""))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test void invalidNullDepartmentNameSetting()
	{
		DepartmentEntity department = new DepartmentEntity();
		assertThatThrownBy(() -> department.name(null))
				.isInstanceOf(NullPointerException.class);
	}
}