module de.ruu.lib.gen.java.fx.bean.editor
{
	exports de.ruu.lib.gen.java.fx.bean.editor;

	requires transitive com.tngtech.archunit;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;

	requires org.slf4j;

	requires transitive de.ruu.lib.gen.java;

	requires de.ruu.lib.archunit;
	requires de.ruu.lib.gen.core;
	requires de.ruu.lib.gen.java.fx.bean;
	requires de.ruu.lib.util;
}