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
	// opens unconditionally: Weld SE runs in the unnamed module (classpath), not as weld.se.shaded,
	// so targeted opens would not reach it. JavaFX (fxml, graphics) also requires open access.
	opens de.ruu.app.pragma.fx;
	opens de.ruu.app.pragma.fx.admin;
	opens de.ruu.app.pragma.fx.task.edit;
	opens de.ruu.app.pragma.fx.task.view;
	opens de.ruu.app.pragma.fx.task.hierarchy;
	opens de.ruu.app.pragma.fx.task.gantt;
	opens de.ruu.app.pragma.fx.task.graph;
	opens de.ruu.app.pragma.fx.taskgroup.edit;
	opens de.ruu.app.pragma.fx.taskgroup.view;
}
