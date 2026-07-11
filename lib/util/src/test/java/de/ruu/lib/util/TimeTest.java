package de.ruu.lib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static de.ruu.lib.util.Time.datesInPeriod;
import static org.assertj.core.api.Assertions.assertThat;

class TimeTest
{
	private static final Logger log = LoggerFactory.getLogger(TimeTest.class);

	@Test void testSortableTimeStamp() { log.debug("sortable time stamp: {}", Time.sortableTimestamp()); }

	@Test void test()
	{
		List<LocalDate> localDates = datesInPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
//		localDates.forEach(d -> log.debug("day in period {}", d));
		assertThat(localDates.size()).isEqualTo(30);
		LocalDate start = LocalDate.of(2025, 1,  1);
		LocalDate date  = LocalDate.of(2025, 1, 31);
		LocalDate end   = LocalDate.of(2025, 1, 31);
		log.debug("{} is in between {} and {}: {}", date, start, end, date.isAfter(start) && date.isBefore(end.plusDays(1)));
	}
}