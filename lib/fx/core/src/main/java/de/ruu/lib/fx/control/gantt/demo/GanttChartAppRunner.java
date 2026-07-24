package de.ruu.lib.fx.control.gantt.demo;

import de.ruu.lib.fx.control.gantt.component.GanttChartController;
import de.ruu.lib.fx.control.gantt.config.GanttChartConfig;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * CDI-based app runner for the Gantt chart demo.
 * 
 * <p>Initializes CDI container, loads FXML with controller injection support,
 * and starts the JavaFX application.
 * 
 * This respects JPMS module boundaries and CDI standards.
 */
public class GanttChartAppRunner extends Application {
	private static final Logger log = LoggerFactory.getLogger(GanttChartAppRunner.class);
	private static SeContainer cdiContainer;
	private static final String GANTT_CHART_FXML = "/de/ruu/lib/fx/control/gantt/GanttChartComponent.fxml";

	public static void main(String[] args) {
		initializeCdiContainer();
		launch(args);
	}

	private static void initializeCdiContainer() {
		try {
			cdiContainer = SeContainerInitializer.newInstance().initialize();
		} catch (IllegalStateException e) {
			String message = e.getMessage();
			if (message != null && message.contains("No valid CDI implementation found")) {
				log.warn("No CDI implementation on runtime path - using local demo data provider fallback.");
				cdiContainer = null;
				return;
			}
			throw e;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		log.info("Starting Gantt Chart with CDI");

		try {
			// Load the Gantt component FXML from main resources
			var fxmlUrl = GanttChartAppRunner.class.getResource(GANTT_CHART_FXML);
			if (fxmlUrl == null) {
				throw new IllegalStateException("GanttChartComponent.fxml not found on classpath at " + GANTT_CHART_FXML);
			}

			log.info("FXML loaded from: {}", fxmlUrl);

			var dataProvider = cdiContainer != null
				? cdiContainer.select(MockGanttDataProvider.class).get()
				: new MockGanttDataProvider();

			// Load FXML with FXMLLoader and pre-initialize controller dependencies
			FXMLLoader loader = new FXMLLoader(fxmlUrl);
			loader.setControllerFactory(controllerType -> {
				if (controllerType == GanttChartController.class) {
					try {
						GanttChartController controller = new GanttChartController();
						var field = GanttChartController.class.getDeclaredField("dataProvider");
						field.setAccessible(true);
						field.set(controller, dataProvider);
						return controller;
					} catch (ReflectiveOperationException e) {
						throw new IllegalStateException("Failed to initialize GanttChartController", e);
					}
				}
				if (cdiContainer != null && cdiContainer.select(controllerType).isResolvable()) {
					return cdiContainer.select(controllerType).get();
				}
				try {
					return controllerType.getDeclaredConstructor().newInstance();
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException("Failed to create controller: " + controllerType.getName(), e);
				}
			});
			Parent root = loader.load();

			// Get controller
			GanttChartController controller = loader.getController();
			
			// Configure and load
			GanttChartConfig config = GanttChartConfig.builder()
				.startDate(LocalDate.of(2026, 3, 22))
				.endDate(LocalDate.of(2026, 3, 28))
				.build();
			
			controller.setConfig(config);

			// Show stage
			Scene scene = new Scene(root, 1200, 700);
			primaryStage.setTitle("Gantt Chart Demo");
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(e -> {
				log.info("Closing application");
				if (cdiContainer != null) cdiContainer.close();
				System.exit(0);
			});

			primaryStage.show();
			log.info("Application started successfully with CDI");
		} catch (Exception e) {
			log.error("Failed to start application", e);
			throw e;
		}
	}
}
