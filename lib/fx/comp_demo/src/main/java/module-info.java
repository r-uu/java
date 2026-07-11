module de.ruu.lib.fx.comp.demo.hierarchy
{
	exports de.ruu.lib.fx.comp.demo.empty;
	exports de.ruu.lib.fx.comp.demo.hierarchy;
	exports de.ruu.lib.fx.comp.demo.hierarchy.sub1;
	exports de.ruu.lib.fx.comp.demo.hierarchy.sub2;

	opens   de.ruu.lib.fx.comp.demo.empty;
	opens   de.ruu.lib.fx.comp.demo.hierarchy;
	opens   de.ruu.lib.fx.comp.demo.hierarchy.sub1;
	opens   de.ruu.lib.fx.comp.demo.hierarchy.sub2;

	requires jakarta.inject;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;

	requires org.apache.logging.log4j;
	requires org.slf4j;

	requires de.ruu.lib.cdi.se;
	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.util;
	requires de.ruu.lib.fx.core;
}