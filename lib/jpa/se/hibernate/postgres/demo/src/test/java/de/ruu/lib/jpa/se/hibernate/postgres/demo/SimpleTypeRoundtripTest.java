package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.core.Entity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTypeRoundtripTest
{
	@Test void roundtrip_preserves_id_version_and_name()
	{
		// Arrange
		Long   id      = 42L;
		Short  version = 3;
		String name    = "roundtrip";

		Entity<Long> stub = new Entity<>()
		{
			@Override public Long  id()      { return id;      }
			@Override public Short version() { return version; }
		};

		// Act: Create DTO from name and stub and convert back to entity
		SimpleTypeDTO    dto = new SimpleTypeDTO(name, stub);
		SimpleTypeEntity e1  = dto.toSource();

		// Assert: Entity has name/id/version from DTO/stub
		assertThat(e1             ).isNotNull();
		assertThat(e1.name()      ).isEqualTo(name);
		assertThat(e1.getId()     ).isEqualTo(id);
		assertThat(e1.getVersion()).isEqualTo(version);

		// Act: Entity -> DTO
		SimpleTypeDTO dto2 = e1.toDTO();

		// Assert: DTO contains same values
		assertThat(dto2             ).isNotNull();
		assertThat(dto2.name()      ).isEqualTo(name);
		assertThat(dto2.getId()     ).isEqualTo(id);
		assertThat(dto2.getVersion()).isEqualTo(version);
	}

	@Test void roundtrip_without_id_and_version_keeps_nulls()
	{
		String name = "new";

		// DTO ohne id/version
		SimpleTypeDTO    dto    = new SimpleTypeDTO(name);
		SimpleTypeEntity entity = dto.toSource();

		assertThat(entity             ).isNotNull();
		assertThat(entity.name()      ).isEqualTo(name);
		assertThat(entity.getId()     ).isEqualTo(null);
		assertThat(entity.getVersion()).isEqualTo(null);

		// Back to DTO
		SimpleTypeDTO dto2 = entity.toDTO();
		assertThat(dto2             ).isNotNull();
		assertThat(dto2.name()      ).isEqualTo(name);
		assertThat(dto2.getId()     ).isEqualTo(null);
		assertThat(dto2.getVersion()).isEqualTo(null);
	}
}