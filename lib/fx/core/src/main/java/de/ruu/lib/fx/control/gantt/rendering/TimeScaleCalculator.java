package de.ruu.lib.fx.control.gantt.rendering;

import de.ruu.lib.fx.control.gantt.config.GanttChartConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Calculates time scale display and pixel-to-date conversions.
 * 
 * <p>Responsible for:
 * - Determining interval size (hours/days/weeks/months) based on date range
 * - Converting dates to pixel coordinates on Canvas
 * - Generating interval labels for header
 * 
 * <p>Example: if showing 2 weeks with 800px width, each day is ~57px.
 */
public class TimeScaleCalculator {
	private static final Logger log = LoggerFactory.getLogger(TimeScaleCalculator.class);

	private final GanttChartConfig config;
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final long totalMinutes;

	// For rendering
	private int numIntervals;
	private TimeUnit intervalUnit;
	private int pixelsPerInterval;

	private enum TimeUnit {
		MINUTE, HOUR, DAY, WEEK, MONTH
	}

	public TimeScaleCalculator(GanttChartConfig config) {
		this.config = config;
		this.startDate = config.getStartDate().atStartOfDay();
		this.endDate = config.getEndDate().atTime(23, 59, 59);
		this.totalMinutes = ChronoUnit.MINUTES.between(startDate, endDate);

		calculateIntervals();
	}

	/**
	 * Determines the best interval size based on total span and typical canvas width.
	 */
	private void calculateIntervals() {
		// Assume ~1000px available width, want 10-30 intervals
		double canvasWidth = 1000;
		long daysSpan = ChronoUnit.DAYS.between(startDate, endDate);

		if (daysSpan <= 1) {
			// Show hours
			intervalUnit = TimeUnit.HOUR;
			numIntervals = (int) ChronoUnit.HOURS.between(startDate, endDate);
			pixelsPerInterval = (int) (canvasWidth / Math.max(1, Math.min(24, numIntervals)));
		} else if (daysSpan <= 7) {
			// Show days
			intervalUnit = TimeUnit.DAY;
			numIntervals = (int) daysSpan;
			pixelsPerInterval = (int) (canvasWidth / Math.max(1, numIntervals));
		} else if (daysSpan <= 60) {
			// Show weeks
			intervalUnit = TimeUnit.WEEK;
			numIntervals = (int) Math.ceil(daysSpan / 7.0);
			pixelsPerInterval = (int) (canvasWidth / Math.max(1, numIntervals));
		} else {
			// Show months
			intervalUnit = TimeUnit.MONTH;
			long monthsSpan = ChronoUnit.MONTHS.between(startDate, endDate);
			numIntervals = (int) Math.max(1, monthsSpan);
			pixelsPerInterval = (int) (canvasWidth / Math.max(1, numIntervals));
		}

		log.debug("Time scale: {} intervals of {}, {} px/interval", numIntervals, intervalUnit, pixelsPerInterval);
	}

	/**
	 * Converts a date to a pixel X coordinate on the Canvas.
	 * Returns pixel position where the date would be displayed.
	 */
	public double dateToPixel(LocalDateTime date) {
		long minutesFromStart = ChronoUnit.MINUTES.between(startDate, date);
		double percentageSpan = (double) minutesFromStart / totalMinutes;
		// Assume 1000px width; adjust as needed
		return 10 + (percentageSpan * 1000);
	}

	/**
	 * Gets the label for the i-th interval in the time scale.
	 */
	public String getIntervalLabel(int intervalIndex) {
		LocalDateTime intervalDate = startDate;

		switch (intervalUnit) {
			case MINUTE:
				intervalDate = startDate.plusMinutes(intervalIndex);
				return intervalDate.format(DateTimeFormatter.ofPattern("HH:mm"));

			case HOUR:
				intervalDate = startDate.plusHours(intervalIndex);
				return intervalDate.format(DateTimeFormatter.ofPattern("HH:00"));

			case DAY:
				intervalDate = startDate.plusDays(intervalIndex);
				return intervalDate.format(DateTimeFormatter.ofPattern("MMM dd"));

			case WEEK:
				intervalDate = startDate.plusWeeks(intervalIndex);
				return "W" + intervalDate.format(DateTimeFormatter.ofPattern("ww"));

			case MONTH:
				intervalDate = startDate.plusMonths(intervalIndex);
				return intervalDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));

			default:
				return "";
		}
	}

	public int getNumIntervals() {
		return numIntervals;
	}

	public TimeUnit getIntervalUnit() {
		return intervalUnit;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}
}
