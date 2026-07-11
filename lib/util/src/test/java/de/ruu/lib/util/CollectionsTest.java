package de.ruu.lib.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static de.ruu.lib.util.Collections.asArray;
import static de.ruu.lib.util.Collections.asList;
import static de.ruu.lib.util.Collections.asSet;
import static org.assertj.core.api.Assertions.assertThat;

class CollectionsTest
{
	@Test void testAsSetWithElements()
	{
		Set<String> set = asSet("A", "B", "C");

		assertThat(set).containsExactlyInAnyOrder("A", "B", "C");
		assertThat(set).hasSize(3);
	}

	@Test void testAsSetWithNull()
	{
		Set<String> set = asSet((String[]) null);

		assertThat(set).isEmpty();
	}

	@Test void testAsListWithElements()
	{
		List<Integer> list = asList(1, 2, 3);

		assertThat(list).containsExactly(1, 2, 3);
	}

	@Test void testAsListWithNull()
	{
		List<Integer> list = asList((Integer[]) null);

		assertThat(list).isEmpty();
	}

	@Test void testAsListWithIterable()
	{
		List<Integer> list = asList(List.of(1, 2, 3));

		assertThat(list).containsExactly(1, 2, 3);
	}

	@Test void testAsListWithNullIterable()
	{
		List<Integer> list = asList((Iterable<Integer>) null);

		assertThat(list).isEmpty();
	}

	@Test void testAsArray()
	{
		List<String> list = List.of("A", "B", "C");
		String[]     arr  = asArray(String.class, list);

		assertThat(arr).isEqualTo(new String[]{"A", "B", "C"});
	}
}