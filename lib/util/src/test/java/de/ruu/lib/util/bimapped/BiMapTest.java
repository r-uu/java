package de.ruu.lib.util.bimapped;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Optional;

class BiMapTest
{
	private class Source { }
	private class Target { }

	@Test void test()
	{
		BiMap biMap = new BiMap();

		Source source = new Source();
		Target target = new Target();

		biMap.put(source, target);

		Optional<Target> optionalTarget = biMap.lookup(source, Target.class);
		Optional<Source> optionalSource = biMap.lookup(target, Source.class);

		assertThat(optionalTarget).isNotNull();
		assertThat(optionalSource).isNotNull();

		assertThat(optionalTarget.isPresent()).isEqualTo(true);
		assertThat(optionalSource.isPresent()).isEqualTo(true);

		assertThat(optionalTarget.get()).isEqualTo(target);
		assertThat(optionalSource.get()).isEqualTo(source);
	}
}