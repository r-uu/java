module de.ruu.lib.gen.java.fx.comp
{
	exports de.ruu.lib.gen.java.fx.comp;
	exports de.ruu.lib.gen.java.fx.comp.demo;

	opens   de.ruu.lib.gen.java.fx.comp.demo;

	requires jakarta.cdi;
	requires jakarta.inject;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.slf4j;

	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.gen.core;
	requires de.ruu.lib.gen.java;
	requires de.ruu.lib.util;

	requires de.ruu.lib.cdi.se;
}