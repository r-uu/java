module de.ruu.lib.fx.control.gantt.demo
{
	requires de.ruu.lib.fx.control.gantt.core;
	requires de.ruu.lib.fx.comp;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;

	requires jakarta.inject;
	requires jakarta.cdi;

	requires org.slf4j;
	requires org.jspecify;

	// Opens for FXML and CDI reflection (unnamed module in IDE)
	opens de.ruu.lib.fx.control.gantt.demo;
}
