package de.ruu.lib.fx.control.gantt.config;

import java.time.LocalDate;

/**
 * Immutable configuration for the Gantt chart component.
 * 
 * <p>Controls rendering parameters like time unit, date range, and styling.
 * Use the builder pattern to create instances:
 * 
 * <pre>
 * GanttChartConfig config = GanttChartConfig.builder()
 *     .timeUnit(TimeUnit.HOURS)
 *     .startDate(LocalDate.now())
 *     .endDate(LocalDate.now().plusMonths(3))
 *     .showWeekends(true)
 *     .rowHeight(40)
 *     .build();
 * </pre>
 */
public class GanttChartConfig
{
	public enum TimeUnit {
		HOURS,
		DAYS,
		WEEKS,
		MONTHS
	}

	private final TimeUnit timeUnit;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final boolean showWeekends;
	private final int rowHeight;
	private final int treeColumnWidth;

	private GanttChartConfig(Builder builder) {
		this.timeUnit = builder.timeUnit;
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.showWeekends = builder.showWeekends;
		this.rowHeight = builder.rowHeight;
		this.treeColumnWidth = builder.treeColumnWidth;
	}

	public TimeUnit getTimeUnit() { return timeUnit; }
	public LocalDate getStartDate() { return startDate; }
	public LocalDate getEndDate() { return endDate; }
	public boolean isShowWeekends() { return showWeekends; }
	public int getRowHeight() { return rowHeight; }
	public int getTreeColumnWidth() { return treeColumnWidth; }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private TimeUnit timeUnit = TimeUnit.HOURS;
		private LocalDate startDate = LocalDate.now();
		private LocalDate endDate = LocalDate.now().plusMonths(3);
		private boolean showWeekends = true;
		private int rowHeight = 40;
		private int treeColumnWidth = 300;

		public Builder timeUnit(TimeUnit timeUnit) {
			this.timeUnit = timeUnit;
			return this;
		}

		public Builder startDate(LocalDate startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder endDate(LocalDate endDate) {
			this.endDate = endDate;
			return this;
		}

		public Builder showWeekends(boolean showWeekends) {
			this.showWeekends = showWeekends;
			return this;
		}

		public Builder rowHeight(int rowHeight) {
			this.rowHeight = rowHeight;
			return this;
		}

		public Builder treeColumnWidth(int treeColumnWidth) {
			this.treeColumnWidth = treeColumnWidth;
			return this;
		}

		public GanttChartConfig build() {
			if (endDate.isBefore(startDate)) {
				throw new IllegalArgumentException("endDate must be >= startDate");
			}
			if (rowHeight < 20) {
				throw new IllegalArgumentException("rowHeight must be >= 20");
			}
			return new GanttChartConfig(this);
		}
	}
}
