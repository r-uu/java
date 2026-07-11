package de.ruu.lib.jasperreports.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic JasperReports Service - Isolated environment for report generation.
 * <p>
 * This service is completely independent of specific report types.
 * It receives:
 * - Template name (e.g., "invoice.jrxml")
 * - Data object (any structure that matches template parameters/fields)
 * - Output format (PDF or DOCX)
 * <p>
 * Focus: Pure report rendering with JasperReports.
 * Business logic and calculations must be done in the data model, not here!
 * <p>
 * Usage:
 * - REST API: http://localhost:8090/api/report/generate
 * - CLI:      java -jar jasperreports-server.jar cli template.jrxml data.json output.pdf
 */
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);


    private static final String TEMPLATES_DIR = "/app/templates";
    private static final String OUTPUT_DIR = "/app/output";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static void main(String[] args) {
        if (args.length > 0 && "cli".equals(args[0])) {
            runCLI(args);
        } else {
            runServer();
        }
    }

    /**
     * Starts REST API server on port 8090.
     */
    private static void runServer() {
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        });

        // Download generated report
        app.get("/api/report/download/{filename}", ctx -> {
            String filename = ctx.pathParam("filename");
            File file = new File(OUTPUT_DIR + "/" + filename);

            if (!file.exists()) {
                ctx.status(404).json(Map.of("error", "File not found"));
                return;
            }

            String contentType = filename.endsWith(".pdf") ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            ctx.contentType(contentType);
            ctx.result(new FileInputStream(file));
        });

        // Health check
        app.get("/health", ctx -> ctx.result("OK"));

        // Report Generation Endpoint (generic)
        app.post("/api/report/generate", ReportService::generateReport);

        // List available templates
        app.get("/api/templates", ReportService::listTemplates);

        app.start(8090);

        log.info("JasperReports Service running on http://localhost:8090");
        log.info("API endpoints: POST /api/report/generate | GET /api/templates | GET /health");
    }

    /**
     * CLI mode for direct report generation.
     */
    private static void runCLI(String[] args)
    {
        if (args.length < 4)
        {
            log.error("Usage: java -jar jasperreports-server.jar cli <template.jrxml> <data.json> <output.pdf|docx>");
            System.exit(1);
        }

        String templatePath = args[1];
        String dataPath = args[2];
        String outputPath = args[3];

        try {
            JasperReport report = JasperCompileManager.compileReport(templatePath);
            Map<String, Object> parameters = new HashMap<>();
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            if (outputPath.endsWith(".pdf")) {
                JasperExportManager.exportReportToPdfFile(print, outputPath);
            } else if (outputPath.endsWith(".docx")) {
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new File(outputPath)));
                exporter.exportReport();
            }

            log.info("Report generated: {}", outputPath);

        } catch (Exception e) {
            log.error("Error generating report", e);
            System.exit(1);
        }
    }

    /**
     * REST Endpoint: Generate report (completely generic).
     * <p>
     * Uses type-safe Java beans instead of Maps.
     * All calculations are performed in the model, not here.
     */
    private static void generateReport(Context ctx) {
        try {
            var body = ctx.bodyAsClass(ReportRequest.class);

            log.info("Request received: template={}, format={}", body.template, body.format);

            String templatePath = TEMPLATES_DIR + "/" + body.template;
            log.debug("Template path: {}", templatePath);

            File templateFile = new File(templatePath);
            if (!templateFile.exists()) {
                throw new FileNotFoundException("Template not found: " + templateFile.getAbsolutePath());
            }

            log.debug("Template file exists: {} bytes, readable: {}", templateFile.length(), templateFile.canRead());

            try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
                String firstLine = reader.readLine();
                log.debug("First line: {}", firstLine != null ? firstLine.substring(0, Math.min(50, firstLine.length())) : "null");
            } catch (Exception e) {
                log.warn("Cannot read first line of template file: {}", e.getMessage());
            }

            JasperReport report;
            if (body.template.endsWith(".jasper")) {
                log.debug("Loading pre-compiled .jasper file...");
                try (InputStream jasperStream = new FileInputStream(templateFile)) {
                    report = (JasperReport) JRLoader.loadObject(jasperStream);
                    log.debug("Pre-compiled template loaded successfully");
                }
            } else {
                log.debug("Compiling .jrxml template...");
                report = JasperCompileManager.compileReport(templatePath);
                log.debug("Template compiled");
            }

            Map<String, Object> parameters = new HashMap<>();
            JRDataSource dataSource = new JREmptyDataSource();

            if (body.data != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = objectMapper.convertValue(body.data, Map.class);

                if (dataMap.containsKey("items")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) dataMap.get("items");
                    dataSource = new JRBeanCollectionDataSource(items);
                    log.debug("Items: {}", items.size());
                }

                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (!"items".equals(entry.getKey())) {
                        parameters.put(entry.getKey(), entry.getValue());
                    }
                }

                log.debug("Parameters: {}", parameters.keySet());
            }

            log.debug("Filling report...");
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);
            log.debug("Report filled");

            String outputFile = OUTPUT_DIR + "/" + UUID.randomUUID() + "." + body.format;

            log.debug("Exporting as {}...", body.format);
            if ("pdf".equals(body.format)) {
                JasperExportManager.exportReportToPdfFile(print, outputFile);
            } else if ("docx".equals(body.format)) {
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new File(outputFile)));
                exporter.exportReport();
            }

            log.info("Export completed: {}", outputFile);

            ctx.status(200).json(Map.of(
                "success", true,
                "outputFile", outputFile,
                "message", "Report generated successfully"
            ));

        } catch (Exception e) {
            log.error("Error during report generation", e);

            ctx.status(500).json(Map.of(
                "success", false,
                "error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(),
                "type", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Lists all available templates.
     */
    private static void listTemplates(Context ctx) {
        try {
            File templatesDir = new File(TEMPLATES_DIR);
            if (!templatesDir.exists() || !templatesDir.isDirectory()) {
                ctx.json(Map.of("templates", List.of()));
                return;
            }

            List<String> templates = Files.list(Paths.get(TEMPLATES_DIR))
                .filter(path -> path.toString().endsWith(".jrxml"))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());

            ctx.json(Map.of(
                "templates", templates,
                "count", templates.size()
            ));

        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Generic request model for REST API.
     * <p>
     * The 'data' field structure must match the JasperReports template's
     * parameter and field definitions.
     */
    static class ReportRequest {
        public String template;        // e.g., "invoice.jrxml", "report.jrxml"
        public String format = "pdf";  // "pdf" or "docx"
        public Object data;            // Generic data object - structure depends on template
    }
}
