package de.ruu.lib.fx.control.gantt.demo;

import de.ruu.lib.fx.control.gantt.component.GanttChartController;
import de.ruu.lib.fx.control.gantt.config.GanttChartConfig;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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

	public static void main(String[] args) {
		// Initialize CDI before launching JavaFX
		cdiContainer = SeContainerInitializer.newInstance().initialize();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		log.info("Starting Gantt Chart with CDI");

		try {
			// Load the FXML from the demo module resources
			ClassLoader cl = GanttChartAppRunner.class.getClassLoader();
			var fxmlUrl = cl.getResource("de/ruu/lib/fx/control/gantt/demo/GanttChartComponent.fxml");
			
			if (fxmlUrl == null) {
				log.warn("FXML not found via classloader, trying alternative paths...");
				// Try alternative paths
				fxmlUrl = cl.getResource("de.ruu/lib/fx/control/gantt/demo/GanttChartComponent.fxml");
				if (fxmlUrl == null) {
					fxmlUrl = cl.getResource("/de/ruu/lib/fx/control/gantt/demo/GanttChartComponent.fxml");
				}
				if (fxmlUrl == null) {
					// List what resources are available
					log.error("FXML still not found. Classpath content sample:");
					throw new IllegalStateException("GanttChartComponent.fxml not found on classpath. ClassLoader: " + cl);
				}
			}

			log.info("FXML loaded from: {}", fxmlUrl);

			// Load FXML with FXMLLoader
			FXMLLoader loader = new FXMLLoader(fxmlUrl);
			BorderPane root = loader.load();
			
			// Get controller
			GanttChartController controller = loader.getController();
			
			// Inject DataProvider from CDI if available, else use MockGanttDataProvider
			var dataProvider = cdiContainer.select(MockGanttDataProvider.class).get();
			
			// Set via reflection (necessary since controller has @Inject)
			var field = GanttChartController.class.getDeclaredField("dataProvider");
			field.setAccessible(true);
			field.set(controller, dataProvider);
			
			// Configure and load
			GanttChartConfig config = GanttChartConfig.builder()
				.startDate(LocalDate.of(2026, 3, 22))
				.endDate(LocalDate.of(2026, 3, 28))
				.build();
			
			controller.setConfig(config);
			var loadTasksMethod = GanttChartController.class.getDeclaredMethod("loadTasks");
			loadTasksMethod.setAccessible(true);
			loadTasksMethod.invoke(controller);

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
