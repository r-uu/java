package de.ruu.lib.util.bimapped.cyclic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SampleBiMappingCyclicTest
{
	@Test void mapSourceToTarget()
	{
		SampleBiMappedSource source = new SampleBiMappedSource("map me");
		SampleBiMappedTarget target = source.toTarget();

		assertThat(target       ).isNotNull();
		assertThat(target.name()).isEqualTo(source.name());

		SampleBiMappedSource remappedSource = target.toSource();

		assertThat(remappedSource).isNotNull();
		assertThat(remappedSource).isEqualTo(source);

		SampleBiMappedTarget remappedTarget = remappedSource.toTarget();

		assertThat(remappedTarget).isNotNull();
		assertThat(remappedTarget).isEqualTo(target);
	}
}