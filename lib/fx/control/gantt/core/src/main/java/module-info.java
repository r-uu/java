module de.ruu.lib.fx.control.gantt.core
{
	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.fx.core;
	requires de.ruu.lib.util;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;

	requires jakarta.inject;
	requires jakarta.cdi;

	requires org.slf4j;
	requires org.jspecify;

	exports de.ruu.lib.fx.control.gantt.api;
	exports de.ruu.lib.fx.control.gantt.component;
	exports de.ruu.lib.fx.control.gantt.config;
	exports de.ruu.lib.fx.control.gantt.rendering;

	opens de.ruu.lib.fx.control.gantt.component to javafx.fxml, org.jboss.weld.se;
	opens de.ruu.lib.fx.control.gantt.config to javafx.fxml, org.jboss.weld.se;
	opens de.ruu.lib.fx.control.gantt.rendering to javafx.fxml, org.jboss.weld.se;
}
