package de.ruu.lib.fx.control.gantt.component;

import de.ruu.lib.fx.comp.DefaultFXCView;

/**
 * FXC View component for Gantt chart visualization.
 * 
 * <p>This component displays a task hierarchy (left) with Gantt chart visualization (right).
 * Manages layout, service injection, and controller lifecycle.
 * 
 * <h3>Usage</h3>
 * <pre>
 * {@code
 * GanttChartComponent gantt = new GanttChartComponent();
 * Scene scene = new Scene(gantt.localRoot(), 1200, 700);
 * primaryStage.setScene(scene);
 * }
 * </pre>
 */
public class GanttChartComponent extends DefaultFXCView<GanttChartComponent, GanttChartService, GanttChartController>
{
}
