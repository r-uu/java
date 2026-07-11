package de.ruu.lib.jpa.core.mapstruct.demo.bidirectional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestMapStruct
{
	@Test void mapEmptyDepartmentDTO()
	{
		DepartmentDTO department = new DepartmentDTO();
		assertThatThrownBy(() -> Mapper.INSTANCE.map(department))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapEmptyDepartmentEntity()
	{
		DepartmentEntity department = new DepartmentEntity();
		assertThatThrownBy(() -> Mapper.INSTANCE.map(department))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapEmptyEmployeeDTO()
	{
		EmployeeDTO employee = new EmployeeDTO();
		assertThatThrownBy(() -> Mapper.INSTANCE.map(employee))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapEmptyEmployeeEntity()
	{
		EmployeeEntity employee = new EmployeeEntity();
		assertThatThrownBy(() -> Mapper.INSTANCE.map(employee))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapInvalidNamedDepartmentDTO()
	{
		String name = null;
		assertThatThrownBy(() -> Mapper.INSTANCE.map(new DepartmentDTO(name)))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapInvalidNamedEmployeeDTO()
	{
		String name = null;
		assertThatThrownBy(() -> Mapper.INSTANCE.map(new EmployeeDTO(new DepartmentDTO("name"), name)))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapInvalidNamedDepartmentEntity()
	{
		String name = null;
		assertThatThrownBy(() -> Mapper.INSTANCE.map(new DepartmentEntity(name)))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapInvalidNamedEmployeeEntity()
	{
		String           name       = null;
		DepartmentEntity department = new DepartmentEntity("name");
		assertThatThrownBy(() -> Mapper.INSTANCE.map(new EmployeeEntity(department, name)))
				.isInstanceOf(NullPointerException.class);
	}

	@Test void mapValidDepartmentDTO()
	{
		String        name       = "name";
		DepartmentDTO department = new DepartmentDTO(name);
		DepartmentEntity departmentEntity = Mapper.INSTANCE.map(department);
		assertThat(departmentEntity       ).isNotNull();
		assertThat(departmentEntity.name()).isEqualTo(name);
	}

	@Test void mapValidDepartmentEntity()
	{
		String           name       = "name";
		DepartmentEntity department = new DepartmentEntity(name);
		DepartmentDTO departmentDTO = Mapper.INSTANCE.map(department);
		assertThat(departmentDTO       ).isNotNull();
		assertThat(departmentDTO.id()  ).isEqualTo(department.getId());
		assertThat(departmentDTO.name()).isEqualTo(name);
	}

	@Test void mapValidEmployeeDTO()
	{
		String         name       = "name";
		DepartmentDTO  department = new DepartmentDTO(name);
		EmployeeDTO    employee   = new EmployeeDTO(department, name);
		EmployeeEntity employeeEntity = Mapper.INSTANCE.map(employee);
		assertThat(employeeEntity                    ).isNotNull();
		assertThat(employeeEntity.name()             ).isEqualTo(name);
		assertThat(employeeEntity.department().name()).isEqualTo(name);
	}

	@Test void mapValidEmployeeEntity()
	{
		String            name       = "name";
		DepartmentEntity  department = new DepartmentEntity(name);
		EmployeeEntity    employee   = new EmployeeEntity(department, name);
		EmployeeDTO employeeDTO = Mapper.INSTANCE.map(employee);
		assertThat(employeeDTO                    ).isNotNull();
		assertThat(employeeDTO.name()             ).isEqualTo(name);
		assertThat(employeeDTO.department().name()).isEqualTo(name);
	}

	@Test void mapValidDepartmentDTOWithEmployees()
	{
		String        name              = "name";
		DepartmentDTO department        = new DepartmentDTO(name);
		int           numberOfEmployees = 3;

		for (int i = 0; i < numberOfEmployees; i++)
		{
			department.add(new EmployeeDTO(department, "name." + i));
		}

		DepartmentEntity departmentEntity = Mapper.INSTANCE.map(department);

		assertThat(departmentEntity                                 ).isNotNull();
		assertThat(departmentEntity.optionalEmployees()             ).isNotNull();
		assertThat(departmentEntity.optionalEmployees().isPresent() ).isEqualTo(true);
		assertThat(departmentEntity.optionalEmployees().get().size()).isEqualTo(numberOfEmployees);
	}

	@Test void mapValidDepartmentEntityWithEmployees()
	{
		String           name              = "name";
		DepartmentEntity department        = new DepartmentEntity(name);
		int              numberOfEmployees = 3;

		for (int i = 0; i < numberOfEmployees; i++)
		{
			department.add(new EmployeeEntity(department, "name." + i));
		}

		DepartmentDTO departmentDTO = Mapper.INSTANCE.map(department);

		assertThat(departmentDTO                                 ).isNotNull();
		assertThat(departmentDTO.optionalEmployees()             ).isNotNull();
		assertThat(departmentDTO.optionalEmployees().isPresent() ).isEqualTo(true);
		assertThat(departmentDTO.optionalEmployees().get().size()).isEqualTo(numberOfEmployees);
	}
}