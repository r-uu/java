module de.ruu.lib.gen.java.fx.bean
{
	exports de.ruu.lib.gen.java.fx.bean;

	requires transitive com.tngtech.archunit;
	requires transitive javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires org.slf4j;

	requires transitive de.ruu.lib.gen.java;

	requires de.ruu.lib.gen.core;
	requires de.ruu.lib.util;
	requires de.ruu.lib.archunit;

}