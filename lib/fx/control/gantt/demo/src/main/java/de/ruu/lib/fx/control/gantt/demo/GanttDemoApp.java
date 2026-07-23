package de.ruu.lib.fx.control.gantt.demo;

/**
 * Entry point for the standalone Gantt chart demo.
 * 
 * <p>Run this to see the Gantt component in action with sample data.
 * No Pragma or business logic dependencies—pure demonstration.
 * 
 * <h3>Running</h3>
 * <pre>
 * java -m de.ruu.lib.fx.control.gantt.demo/de.ruu.lib.fx.control.gantt.demo.GanttDemoApp
 * </pre>
 * 
 * Or from Maven:
 * <pre>
 * mvn -pl lib/fx/control/gantt/demo clean javafx:run
 * </pre>
 */
public class GanttDemoApp {
	public static void main(String[] args) {
		GanttChartAppRunner.main(args);
	}
}

