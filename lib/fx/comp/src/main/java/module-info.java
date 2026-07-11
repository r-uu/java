module de.ruu.lib.fx.comp
{
	exports de.ruu.lib.fx.comp;

  opens   de.ruu.lib.fx.comp;

	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires jakarta.cdi;
	requires org.slf4j;
	requires de.ruu.lib.cdi.common;
	requires de.ruu.lib.cdi.se;
//	requires de.ruu.lib.fx.core;
	requires de.ruu.lib.util;
}