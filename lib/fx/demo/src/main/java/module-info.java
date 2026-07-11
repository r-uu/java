module de.ruu.lib.fx.demo
{
	exports de.ruu.lib.fx.demo;
	exports de.ruu.lib.fx.demo.bean;
	exports de.ruu.lib.fx.demo.comp.main;
	exports de.ruu.lib.fx.demo.gen.input;

	opens de.ruu.lib.fx.demo.bean;
	opens de.ruu.lib.fx.demo.comp.main;
	opens de.ruu.lib.fx.demo.gen;

	requires com.tngtech.archunit;
	requires javafx.controls;
	requires javafx.fxml;
	requires org.slf4j;

	requires transitive javafx.base;

	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.gen.core;
	requires de.ruu.lib.gen.java.fx.bean;
	requires de.ruu.lib.gen.java.fx.comp;
	requires de.ruu.lib.gen.java.fx.tableview;

}