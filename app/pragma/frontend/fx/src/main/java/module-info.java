module de.ruu.app.pragma.fx
{
	requires de.ruu.app.pragma.core;
	requires de.ruu.app.pragma.bean;
	requires de.ruu.app.pragma.dto;
	requires de.ruu.app.pragma.client;

	requires de.ruu.lib.postgres.ui;

	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.fx.core;
	requires de.ruu.lib.cdi.se;
	requires de.ruu.lib.cdi.common;
	requires de.ruu.lib.util;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires java.net.http;

	requires jakarta.inject;
	requires jakarta.cdi;

	requires org.jspecify;
	requires org.apache.logging.log4j;
	requires org.kordamp.ikonli.javafx;

	// no exports: no other module imports from de.ruu.app.pragma.fx at compile time
	// opens for CDI (Weld) bean discovery/proxy generation, FXML controller injection,
	// and JavaFX Application subclass instantiation (javafx.graphics)
	opens de.ruu.app.pragma.fx                to javafx.graphics, javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.admin          to javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.task.edit      to javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.task.view      to javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.task.hierarchy to javafx.graphics, javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.task.gantt     to javafx.graphics, javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.task.graph     to javafx.graphics, javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.taskgroup.edit to javafx.fxml, weld.se.shaded;
	opens de.ruu.app.pragma.fx.taskgroup.view to javafx.fxml, weld.se.shaded;
}
