/**
 * Reusable Gantt chart control for JavaFX.
 * 
 * <h2>Architecture</h2>
 * <p>
 * The component is built on three layers:
 * </p>
 * <ul>
 *   <li><strong>API ({@link de.ruu.lib.fx.control.gantt.api})</strong>
 *     — Domain interfaces for tasks, hierarchy, and data mutations.
 *     Implementation-agnostic, so your application can adapt any data model.
 *   </li>
 *   <li><strong>Component ({@link de.ruu.lib.fx.control.gantt.component})</strong>
 *     — MVC-based FXC component. Controllers handle UI logic, services coordinate data.
 *   </li>
 *   <li><strong>Config ({@link de.ruu.lib.fx.control.gantt.config})</strong>
 *     — Configuration for rendering (time unit, date range, styling).
 *   </li>
 * </ul>
 * 
 * <h2>Quick Start</h2>
 * <pre>
 * // 1. Adapt your tasks to the GanttTask interface
 * class MyTaskAdapter implements GanttTask { ... }
 * 
 * // 2. Implement a data provider
 * @Dependent
 * class MyGanttDataProvider implements GanttDataProvider { ... }
 * 
 * // 3. Configure and create the component
 * GanttChartConfig config = GanttChartConfig.builder()
 *     .timeUnit(TimeUnit.HOURS)
 *     .build();
 * 
 * GanttChartComponent gantt = new GanttChartComponent()
 *     .setConfig(config)
 *     .setDataProvider(myDataProvider);
 * 
 * // 4. Add to your scene
 * primaryStage.setScene(new Scene(gantt.getRoot(), 1200, 600));
 * </pre>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Interface-driven</strong> — No dependency on your domain model</li>
 *   <li><strong>FXC-compatible</strong> — Uses standard MVC patterns</li>
 *   <li><strong>CDI-injectable</strong> — DataProvider is injectable</li>
 *   <li><strong>Testable</strong> — Mock implementations can be provided in tests</li>
 * </ul>
 * 
 * @see de.ruu.lib.fx.control.gantt.api.GanttTask
 * @see de.ruu.lib.fx.control.gantt.api.GanttDataProvider
 * @see de.ruu.lib.fx.control.gantt.config.GanttChartConfig
 */
package de.ruu.lib.fx.control.gantt;
