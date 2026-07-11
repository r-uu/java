module de.ruu.lib.gen.java.fx.tableview
{
	exports de.ruu.lib.gen.java.fx.tableview;

	requires transitive com.tngtech.archunit;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires org.slf4j;

	requires de.ruu.lib.gen.core;
	requires de.ruu.lib.gen.java;
	requires de.ruu.lib.gen.java.fx.bean;
	requires de.ruu.lib.util;
	requires de.ruu.lib.archunit;

}