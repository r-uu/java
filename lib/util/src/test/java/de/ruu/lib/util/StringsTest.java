package de.ruu.lib.util;

import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.Strings.allTrimChars;
import static de.ruu.lib.util.Strings.lFillCharsTargetLength;
import static de.ruu.lib.util.Strings.rFillCharsTargetLength;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringsTest
{
	@Test void testAllTrimChars__xay_xy__a               () { assertThat(allTrimChars("xay"        , "xy"  )).isEqualTo("a"        ); }
	@Test void testAllTrimChars__abcxxxcba_bac__xxx      () { assertThat(allTrimChars("abcxxxcba"  , "bac" )).isEqualTo("xxx"      ); }
	@Test void testAllTrimChars__abcxxxcba_x__abcxxxcba  () { assertThat(allTrimChars("abcxxxcba"  , "x"   )).isEqualTo("abcxxxcba"); }
	@Test void testAllTrimChars__null_cab__null          () { assertThat(allTrimChars(null         , "cab" )).isEqualTo(null       ); }
	@Test void testAllTrimChars__aaaxxxaaa_a__xxx        () { assertThat(allTrimChars("aaaxxxaaa"  , "a"   )).isEqualTo("xxx"      ); }
	@Test void testLFillCharsTargetLength__empty_a_3__aaa() { assertThat(lFillCharsTargetLength("" , 'a', 3)).isEqualTo("aaa"      ); }
	@Test void testLFillCharsTargetLength__x_a_4__aaax   () { assertThat(lFillCharsTargetLength("x", 'a', 4)).isEqualTo("aaax"     ); }
	@Test void testRFillCharsTargetLength__empty_a_3_aaa () { assertThat(rFillCharsTargetLength("" , 'a', 3)).isEqualTo("aaa"      ); }
	@Test void testRFillCharsTargetLength__x_a_4__xaaa   () { assertThat(rFillCharsTargetLength("x", 'a', 4)).isEqualTo("xaaa"     ); }

	@Test void test()
	{
		String input  = "" + ((char) 10) + ((char) 160);
		String toTrim = "" + ((char) 10) + ((char) 160);
		assertThat(allTrimChars(input, toTrim).length()).isEqualTo(0);
	}
	
	@Test void testNormalizeLineSeparator()
	{
		assertThat(Strings.normaliseLineSeparator("\n")).isEqualTo(LS);
		assertThat(Strings.normaliseLineSeparator("\r\n")).isEqualTo(LS);
		assertThat(Strings.normaliseLineSeparator(LS)).isEqualTo(LS);
		assertThat(Strings.normaliseLineSeparator(SystemProperties.lineSeparator())).isEqualTo(LS);
	}
}