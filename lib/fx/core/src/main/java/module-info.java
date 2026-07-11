module de.ruu.lib.fx.core
{
	exports de.ruu.lib.fx;
	exports de.ruu.lib.fx.control;
	exports de.ruu.lib.fx.control.autocomplete;
	exports de.ruu.lib.fx.control.autocomplete.textfield.v1;
	exports de.ruu.lib.fx.control.autocomplete.textfield.v2;
	exports de.ruu.lib.fx.control.buttons;
	exports de.ruu.lib.fx.control.dialog;
	exports de.ruu.lib.fx.control.textfield.number;
	exports de.ruu.lib.fx.control.treeview;
	exports de.ruu.lib.fx.css;
	exports de.ruu.lib.fx.control.autocomplete.textfield;

	opens de.ruu.lib.fx.control.buttons;

	requires de.ruu.lib.util;
	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.cdi.se;

	requires transitive javafx.controls;
	requires transitive javafx.fxml;
	requires java.desktop;
	// requires javafx.graphics;

	requires org.apache.logging.log4j;

	requires org.slf4j;
	requires de.ruu.lib.cdi.common;
	requires javafx.graphics;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
}
