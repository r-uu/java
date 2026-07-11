package de.ruu.lib.fx.control.textfield.number;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.LINUX;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

@DisabledOnOs(LINUX)
class TestRegExpForBigDecimal
{
	private final Pattern p = Pattern.compile("-?\\d+(,\\d+)?");

	@Test void testInteger()
	{
		final Matcher m = p.matcher("-10");

		assertThat(m.find()).isTrue();
		assertThat(m.group()).isEqualTo("-10");
	}

	@Test void testDecimal()
	{
//		Matcher m = p.matcher("10.99");
		final Matcher m = p.matcher("10,99");

		assertThat(m.find()).isTrue();
//		assertThat(m.group()).isEqualTo("10.99");
		assertThat(m.group()).isEqualTo("10,99");
	}
}