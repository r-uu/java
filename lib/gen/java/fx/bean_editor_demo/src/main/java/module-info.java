module de.ruu.lib.gen.java.fx.bean.editor.demo
{
	exports de.ruu.lib.gen.java.fx.bean.editor.demo;
	opens   de.ruu.lib.gen.java.fx.bean.editor.demo;

	requires com.tngtech.archunit;

	requires org.apache.logging.log4j;
	requires org.slf4j;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;

	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.gen.core;
	requires de.ruu.lib.gen.java;
	requires de.ruu.lib.gen.java.fx.bean;
	requires de.ruu.lib.gen.java.fx.comp;
	requires de.ruu.lib.gen.java.fx.bean.editor;
}