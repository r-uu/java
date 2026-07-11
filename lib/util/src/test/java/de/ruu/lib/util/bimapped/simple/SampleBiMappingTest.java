package de.ruu.lib.util.bimapped.simple;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SampleBiMappingTest
{
	@Test void mapSourceToTarget()
	{
		SampleBiMappedSource source = new SampleBiMappedSource("map me");
		SampleBiMappedTarget target;

		Optional<SampleBiMappedTarget> optionalTarget = source.map(SampleBiMappedTarget.class);

		if (optionalTarget.isPresent())
		{
			target = optionalTarget.get();
		}
		else
		{
			target = new SampleBiMappedTarget(source);
		}

		assertThat(target       ).isNotNull();
		assertThat(target.name()).isEqualTo(source.name());

		SampleBiMappedSource remapped = null;

		Optional<SampleBiMappedSource> optionalSource = target.map(SampleBiMappedSource.class);

		if (optionalSource.isPresent())
		{
			remapped = optionalSource.get();
		}

		assertThat(remapped).isNotNull();
		assertThat(remapped).isEqualTo(source);
	}
}