module de.ruu.lib.postgres.ui
{
	exports de.ruu.lib.postgres.toolbox.ui;

	// Open package for CDI bean discovery and proxy generation (Weld SE) and JavaFX reflection
	// - javafx.fxml: for FXML controller instantiation
	// - weld.se.shaded: for CDI @Inject and @ApplicationScoped proxying
	opens de.ruu.lib.postgres.toolbox.ui;

	requires de.ruu.lib.fx.comp;
	requires de.ruu.lib.util.config.mp;
	requires javafx.fxml;
	requires javafx.controls;
	requires jakarta.inject;
	requires microprofile.config.api;

	requires org.slf4j;
	requires de.ruu.lib.postgres;
	requires de.ruu.lib.fx.core;
	requires de.ruu.lib.util;
	requires de.ruu.lib.cdi.se;
	requires de.ruu.lib.cdi.common;
}
