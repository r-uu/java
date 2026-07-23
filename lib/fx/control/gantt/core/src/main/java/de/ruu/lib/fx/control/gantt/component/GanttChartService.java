package de.ruu.lib.fx.control.gantt.component;

import de.ruu.lib.fx.comp.FXCService;

/**
 * Service interface for the Gantt chart component.
 * 
 * <p>Provides access to Gantt chart operations like:
 * - Configuring the data source
 * - Refreshing task display
 * - Handling task selections and edits
 * 
 * <p>Implementations inject the {@link de.ruu.lib.fx.control.gantt.api.GanttDataProvider}
 * and manage the component lifecycle.
 */
public interface GanttChartService extends FXCService {
}
