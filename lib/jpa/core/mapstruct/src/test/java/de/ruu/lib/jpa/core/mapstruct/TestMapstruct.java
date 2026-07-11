package de.ruu.lib.jpa.core.mapstruct;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestMapstruct
{
	@Test
	void test()
	{
		SimpleMappedEntity source = new SimpleMappedEntity("name");
		SimpleMappedDTO target = Mapper.INSTANCE.map(source); // mapping
		SimpleMappedEntity remapped = target.toSource(); // re-mapping
		isEquals(target, source);
		isEquals(target, remapped);
	}

	private void isEquals(SimpleMappedDTO source, SimpleMappedEntity target)
	{
		assertThat(source.name()).isEqualTo(target.name());
	}
}