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

	requires jakarta.inject;
	requires jakarta.cdi;

	requires org.jspecify;
	requires org.apache.logging.log4j;
	requires org.kordamp.ikonli.javafx;

	exports de.ruu.app.pragma.fx;
	exports de.ruu.app.pragma.fx.task.gantt;
	exports de.ruu.app.pragma.fx.task.graph;
	exports de.ruu.app.pragma.fx.task.edit;
	exports de.ruu.app.pragma.fx.task.view;
	exports de.ruu.app.pragma.fx.taskgroup.edit;
	exports de.ruu.app.pragma.fx.taskgroup.view;

	// open for CDI (Weld) bean discovery, proxy generation, and FXML controller injection
	opens de.ruu.app.pragma.fx;
	opens de.ruu.app.pragma.fx.task.edit;
	opens de.ruu.app.pragma.fx.task.view;
	opens de.ruu.app.pragma.fx.task.hierarchy;
	opens de.ruu.app.pragma.fx.task.gantt;
	opens de.ruu.app.pragma.fx.task.graph;
	opens de.ruu.app.pragma.fx.taskgroup.edit;
	opens de.ruu.app.pragma.fx.taskgroup.view;
}
