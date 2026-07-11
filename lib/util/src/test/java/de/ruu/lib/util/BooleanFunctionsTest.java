package de.ruu.lib.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.ruu.lib.util.BooleanFunctions.is;
import static de.ruu.lib.util.BooleanFunctions.isNot;
import static org.assertj.core.api.Assertions.assertThat;

class BooleanFunctionsTest
{
	@Test void testIsTrue    () { assertThat(is   (true )).isEqualTo(true ); }
	@Test void testIsFalse   () { assertThat(is   (false)).isEqualTo(false); }
	@Test void testIsNotTrue () { assertThat(isNot(true )).isEqualTo(false); }
	@Test void testIsNotFalse() { assertThat(isNot(false)).isEqualTo(true ); }
}