package de.ruu.lib.jasperreports.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generic Java client for JasperReports Service.
 * <p>
 * This client is independent of specific report types and can generate
 * any report by providing:
 * - Template name (e.g., "invoice.jrxml")
 * - Data object (any POJO)
 * - Output format (PDF or DOCX)
 * <p>
 * Usage Example:
 * <pre>
 * var client = new JasperReportsClient();
 *
 * var data = new MyReportData();
 * // ... populate data ...
 *
 * Path pdf = client.generateReport("mytemplate.jrxml", data, ReportFormat.PDF);
 * </pre>
 */
public class JasperReportsClient
{
	private static final String DEFAULT_SERVICE_URL = "http://localhost:8090";
	private static final String GENERATE_ENDPOINT   = "/api/report/generate";

	private final String     serviceUrl;
	private final HttpClient httpClient;
	private final Gson       gson;

	public JasperReportsClient()
	{
		this(DEFAULT_SERVICE_URL);
	}

	public JasperReportsClient(String serviceUrl)
	{
		this.serviceUrl = serviceUrl;
		this.httpClient = HttpClient.newHttpClient();

    gson =
        new GsonBuilder()
            .registerTypeAdapter(
                LocalDate.class,
                (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                                                      (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .setPrettyPrinting()
            .create();
	}

	/**
	 * Generates a report with the specified template, data, and format.
	 * <p>
	 * This is the main method - completely generic and independent of report type.
	 *
	 * @param template Template filename (e.g., "invoice.jrxml", "report.jrxml")
	 * @param data     Data object (any POJO - will be serialized to JSON automatically)
	 * @param format   Output format (PDF or DOCX)
	 * @return Path to the generated file
	 * @throws IOException          If report generation fails or service is unavailable
	 * @throws InterruptedException If HTTP request is interrupted
	 */
	public Path generateReport(String template, Object data, ReportFormat format)
			throws IOException, InterruptedException
	{

		// Create request body
		var requestBody = new ReportRequest();
		requestBody.template = template;
		requestBody.format = format.name().toLowerCase();
		requestBody.data = data;

		String jsonBody = gson.toJson(requestBody);

		// HTTP Request
		HttpRequest request = HttpRequest.newBuilder()
				                      .uri(URI.create(serviceUrl + GENERATE_ENDPOINT))
				                      .header("Content-Type", "application/json")
				                      .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				                      .build();

		// Send request
		HttpResponse<String> response = httpClient.send(request,
		                                                HttpResponse.BodyHandlers.ofString());

		// Process response
		if (response.statusCode() != 200)
		{
			throw new IOException("Report generation failed: " + response.body());
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> result = gson.fromJson(response.body(), Map.class);

		if (!(Boolean) result.get("success"))
		{
			throw new IOException("Report generation failed: " + result.get("error"));
		}

		String outputFile = (String) result.get("outputFile");

		// Convert Docker path to host path
		// /app/output/xxx.pdf -> root/sandbox/office/microsoft/word/jasperreports/server/output/xxx.pdf
		String fileName = outputFile.substring(outputFile.lastIndexOf('/') + 1);
		Path localPath = Paths.get("root/sandbox/office/microsoft/word/jasperreports/server/output", fileName);

		// Wait for file to become available
		int retries = 10;
		while (retries-- > 0 && !Files.exists(localPath))
		{
			Thread.sleep(100);
		}

		if (!Files.exists(localPath))
		{
			throw new IOException("Generated file not found: " + localPath);
		}

		return localPath;
	}

	/**
	 * Checks if the JasperReports service is available.
	 *
	 * @return true if service responds to health check, false otherwise
	 */
	public boolean serviceAvailable()
	{
		try
		{
			HttpRequest request = HttpRequest.newBuilder()
					                      .uri(URI.create(serviceUrl + "/health"))
					                      .GET()
					                      .build();

			HttpResponse<String> response = httpClient.send(request,
			                                                HttpResponse.BodyHandlers.ofString());

			return response.statusCode() == 200;
		} catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Convenience method: Generates report as PDF.
	 */
	public Path generatePDF(String template, Object data) throws IOException, InterruptedException
	{
		return generateReport(template, data, ReportFormat.PDF);
	}

	/**
	 * Convenience method: Generates report as DOCX.
	 */
	public Path generateDOCX(String template, Object data) throws IOException, InterruptedException
	{
		return generateReport(template, data, ReportFormat.DOCX);
	}

	// ==================== Inner Classes ====================

	/**
	 * Supported output formats.
	 */
	public enum ReportFormat
	{
		PDF,
		DOCX
	}

	/**
	 * Internal request model for REST API.
	 */
	static class ReportRequest
	{
		public String template;        // e.g., "invoice.jrxml"
		public String format = "pdf";  // "pdf" or "docx"
		public Object data;            // Mapped to report-specific bean (e.g., InvoiceData)
	}
}

