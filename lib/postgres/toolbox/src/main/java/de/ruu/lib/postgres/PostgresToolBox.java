package de.ruu.lib.postgres;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ruu.lib.util.config.mp.ConfigFileInitializer;
import de.ruu.lib.util.config.mp.WritableFileConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresToolBox
{
	private static final Logger log = LoggerFactory.getLogger(PostgresToolBox.class);

	private PostgresToolBox() { throw new UnsupportedOperationException("utility class, not meant to be instantiated"); }

	public static void backup(
			Path executable, String host, int port, String dbName, String username, String password, Path backupFile)
			throws IOException, InterruptedException
	{
		List<String> command = List.of
				(
						executable.toString(),
						"-h", host,
						"-p", String.valueOf(port),
						"-U", username,
						"-Fc", // custom format
						"-f", backupFile.toAbsolutePath().toString(),
						dbName
				);

		runCommandWithEnv(command, Map.of("PGPASSWORD", password));
	}

	public static void restore(
			Path executable, String host, int port, String dbName, String username, String password, Path backupFile)
			throws IOException, InterruptedException
	{
		List<String> command = List.of
				(
						executable.toString(),
						"-h", host,
						"-p", String.valueOf(port),
						"-U", username,
						"-d", dbName,
						"-c",          // clean (drop objects before recreating)
						"--if-exists", // ignore errors if objects don't exist
						backupFile.toAbsolutePath().toString()
				);

		runCommandWithEnv(command, Map.of("PGPASSWORD", password));
	}

	private static void runCommandWithEnv(List<String> command, Map<String, String> env)
			throws IOException, InterruptedException
	{
		log.debug("executing command: {}", String.join(" ", command));

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.environment().putAll(env);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		// Log output
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
		{
			reader.lines().forEach(System.out::println);
		}
		int exitCode = process.waitFor();
		if (exitCode != 0) throw new IOException("command failed with exit code " + exitCode);
	}

	/**
	 * Creates a postgres utility config file with standard default values.
	 *
	 * <p>
	 * Default values included:
	 * <ul>
	 *   <li>postgres.host               = localhost
	 *   <li>postgres.port               = 5432
	 *   <li>postgres.database           = mydb
	 *   <li>postgres.username           = admin
	 *   <li>postgres.password           = changeme
	 *   <li>postgres.schema             = public
	 *   <li>postgres.ssl.enabled        = false
	 *   <li>postgres.connection.timeout = 30000
	 *   <li>postgres.max.pool.size      = 10
	 * </ul>
	 *
	 * @return WritableFileConfigSource instance for the postgres config
	 * @example <pre>
	 *   {@code
	 *     // Simple usage in your application startup
	 *     WritableFileConfigSource postgresConfig = ConfigFileInitializer.initializePostgresUtilConfig();
	 *
	 *     // Read configuration
	 *     String host = postgresConfig.getValue("postgres.host");
	 *     String port = postgresConfig.getValue("postgres.port");
	 *    }
	 *  </pre>
	 */
	public static WritableFileConfigSource initializePostgresUtilConfig()
	{
		return initializePostgresUtilConfig("postgresutil.config.properties");
	}

	/**
	 * Creates a postgres utility config file with standard default values at a custom location.
	 *
	 * @param configFilePath Custom path for the config file
	 * @return WritableFileConfigSource instance for the postgres config
	 */
	public static WritableFileConfigSource initializePostgresUtilConfig(String configFilePath)
	{
		Map<String, String> defaults = new HashMap<>();
		defaults.put("postgres.host", "localhost");
		defaults.put("postgres.port", "5432");
		defaults.put("postgres.database", "mydb");
		defaults.put("postgres.username", "admin");
		defaults.put("postgres.password", "changeme");
		defaults.put("postgres.schema", "public");
		defaults.put("postgres.ssl.enabled", "false");
		defaults.put("postgres.connection.timeout", "30000");
		defaults.put("postgres.max.pool.size", "10");

		return ConfigFileInitializer.initializeConfigFile(configFilePath, defaults);
	}
}
