# MicroProfile Config Module (mp.config)

## Overview

This module provides a writable file-based configuration source for **MicroProfile Config**. It extends the standard MicroProfile Config API to support **reading and writing** configuration properties from/to a file at runtime.

## Purpose

MicroProfile Config normally treats configuration sources as read-only. This module adds a `WritableFileConfigSource` that:
- Reads configuration from a properties file
- Allows runtime modification of configuration values
- Persists changes back to the file
- Integrates seamlessly with the MicroProfile Config framework

## Components

### WritableFileConfigSource

A `ConfigSource` implementation that stores configuration in a properties file and supports write operations.

### ConfigFileInitializer

A utility class that simplifies creating and initializing configuration files with default values. This is especially useful for:
- Creating config files in the project root when they don't exist
- Populating new config files with sensible defaults
- Ensuring all required properties exist without overwriting user customizations

#### Key Features

1. **Lazy Initialization**: Properties are loaded only when first accessed to avoid bootstrap recursion issues
2. **File-Based Storage**: Reads from and writes to a configurable properties file
3. **Runtime Modification**: Supports adding, updating, and removing properties
4. **Automatic Persistence**: Changes are immediately saved to the file
5. **High Priority**: Uses ordinal 500 to override default config sources

#### Configuration

The file location is determined by (in order of priority):
1. System property `config.file.name`
2. Property `config.file.name` in `META-INF/microprofile-config.properties`
3. Default value: `config/application.properties`

#### Usage Example

```java
// Get the config source
Optional<WritableFileConfigSource> source = 
    ConfigSourceUtil.activeWritableFileConfigSource();

if (source.isPresent()) {
    WritableFileConfigSource config = source.get();
    
    // Read a property
    String value = config.getValue("app.name");
    
    // Set a new property (automatically saved to file)
    config.setProperty("app.version", "1.0.0");
    
    // Update an existing property
    config.setProperty("app.name", "MyApp");
    
    // Remove a property (automatically saved to file)
    config.removeProperty("old.property");
    
    // Get all property names
    Set<String> propertyNames = config.getPropertyNames();
    
    // Get all properties as a map
    Map<String, String> properties = config.getProperties();
}
```

#### Methods

- **`getValue(String key)`**: Returns the value for the given property key, or null if not found
- **`getPropertyNames()`**: Returns all property keys
- **`getProperties()`**: Returns an unmodifiable map of all properties
- **`setProperty(String key, String value)`**: Sets a property and saves to file
- **`removeProperty(String key)`**: Removes a property and saves to file
- **`save()`**: Manually saves all properties to file (called automatically by set/remove)
- **`getName()`**: Returns the name of this config source including the file path
- **`getOrdinal()`**: Returns 500 (higher than default sources)

### ConfigSourceUtil

A utility class to find the active `WritableFileConfigSource` in the current MicroProfile Config.

#### Usage Example

```java
// Find the writable config source
Optional<WritableFileConfigSource> optSource = 
    ConfigSourceUtil.activeWritableFileConfigSource();

optSource.ifPresent(source -> {
    // Use the config source
    source.setProperty("key", "value");
});
```

#### Methods

- **`activeWritableFileConfigSource()`**: Returns the first `WritableFileConfigSource` found in the active config sources, or an empty Optional if none exists

## Creating Config Files with Default Values

### Quick Start: Create postgresutil.config

The easiest way to create a config file with default values in your project root:

```java

import de.ruu.lib.util.config.mp.WritableFileConfigSource;

// Create postgresutil.config in project root with default values
WritableFileConfigSource config =
		PostgresUtil.initializePostgresUtilConfig();

// The file is now created at: postgresutil.config
// It contains default values for postgres connection settings

		// Read values
		String host = config.getValue("postgres.host");     // "localhost"
		String port = config.getValue("postgres.port");     // "5432"

// Modify values (automatically saved to file)
config.

		setProperty("postgres.host","db.example.com");
config.

		setProperty("postgres.port","5433");
```

### Creating Custom Config Files

```java
import java.util.HashMap;
import java.util.Map;

// Define your default values
Map<String, String> defaults = new HashMap<>();
defaults.put("app.name", "MyApp");
defaults.put("app.version", "1.0.0");
defaults.put("app.debug", "false");

// Create config file in project root
WritableFileConfigSource config = ConfigFileInitializer.initializeConfigFile(
    "myapp.config",
    defaults
);

// If file already exists, only missing properties are added
// Existing values are never overwritten
```

### Safe Initialization (Preserve User Changes)

The `ConfigFileInitializer` automatically preserves existing values:

```java
// First run: creates file with defaults
ConfigFileInitializer.initializePostgresUtilConfig();
// Creates: postgresutil.config with postgres.host=localhost

// User manually edits file: postgres.host=production-db

// Second run: preserves user changes, adds any new defaults
ConfigFileInitializer.initializePostgresUtilConfig();
// Result: postgres.host=production-db (user value preserved)
```

### Ensuring Required Properties

For application updates where new config properties are needed:

```java
WritableFileConfigSource config = /* ... */;

Map<String, String> requiredDefaults = new HashMap<>();
requiredDefaults.put("new.feature.enabled", "true");
requiredDefaults.put("new.cache.size", "1000");

// Adds only missing properties
int added = ConfigFileInitializer.ensureRequiredProperties(
    config, 
    requiredDefaults
);

System.out.println("Added " + added + " new properties");
```

## How It Works

### Bootstrap Process

1. The `WritableFileConfigSource` is registered via the Java ServiceLoader mechanism (declared in `module-info.java`)
2. MicroProfile Config automatically discovers and registers it during initialization
3. The first time a property is accessed, the config source:
   - Reads the `config.file.name` property from system properties or bootstrap config
   - Opens the specified file
   - Loads all properties into memory
   - Marks itself as loaded

### File Format

The configuration file uses standard Java properties format:

```properties
# Comment
key1=value1
key2=value2
app.name=MyApplication
app.version=1.0.0
```

### Thread Safety

The `WritableFileConfigSource` uses a `ConcurrentHashMap` internally to store properties, making it thread-safe for concurrent reads and writes.

## Integration with MicroProfile Config

This config source integrates with the standard MicroProfile Config API:

```java
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

// Standard MP Config usage
Config config = ConfigProvider.getConfig();
String value = config.getValue("app.name", String.class);

// The WritableFileConfigSource is automatically included
// and can override values from other sources due to its high ordinal
```

## Dependencies

This module requires:
- **MicroProfile Config API** (`microprofile-config-api`)
- **SmallRye Config** (implementation of MicroProfile Config)
- **Jakarta CDI API** (`jakarta.enterprise.cdi-api`)
- **Lombok** (for logging annotations, compile-time only)
- **SLF4J** (for logging)

## Module Definition

The module is defined in `module-info.java`:

```java
module de.ruu.lib.util.config {
    exports de.ruu.lib.util.config.mp;
    
    provides org.eclipse.microprofile.config.spi.ConfigSource 
        with de.ruu.lib.util.config.mp.WritableFileConfigSource;
    
    requires de.ruu.lib.util;
    requires transitive jakarta.cdi;
    requires transitive microprofile.config.api;
    requires static lombok;
    requires org.slf4j;
}
```

## Testing

The module includes comprehensive JUnit tests using Hamcrest matchers:

### WritableFileConfigSourceTest

Tests for the main config source implementation:
- Lazy loading of properties
- Reading property values
- Setting new properties
- Updating existing properties
- Removing properties
- File persistence
- Handling missing config files
- Thread safety of property maps

### ConfigSourceUtilTest

Tests for the utility class:
- Finding the active WritableFileConfigSource
- Verification that the found source is functional
- Singleton behavior
- Integration with MicroProfile Config

### Running Tests

```bash
# Run all tests in the module
mvn test -pl lib/mp/config

# Run a specific test class
mvn test -pl lib/mp/config -Dtest=WritableFileConfigSourceTest
```

## Common Use Cases

### 1. Application Settings Management

```java
// Load settings
WritableFileConfigSource config = /* ... */;
String theme = config.getValue("ui.theme");

// Save user preference
config.setProperty("ui.theme", "dark");
```

### 2. Runtime Configuration Updates

```java
// Update configuration without restarting the application
config.setProperty("cache.size", "500");
config.setProperty("feature.enabled", "true");
```

### 3. Configuration Migration

```java
// Remove deprecated properties
config.removeProperty("old.setting");

// Add new properties with default values
if (config.getValue("new.setting") == null) {
    config.setProperty("new.setting", "default");
}
```

## Important Notes

### Avoiding Bootstrap Recursion

The implementation carefully avoids using `ConfigProvider.getConfig()` during initialization to prevent bootstrap recursion. Instead, it:
1. Checks system properties first
2. Falls back to reading `META-INF/microprofile-config.properties` directly
3. Uses a default value as last resort

### File Creation

If the configured file doesn't exist:
- The config source logs a warning
- It continues with an empty property set
- The file will be created when the first property is set

### Ordinal Priority

With an ordinal of 500, this config source:
- Overrides default MicroProfile config files (ordinal 100)
- Overrides environment variables (ordinal 300)
- Can be overridden by system properties (ordinal 400) - **Note: System properties typically have ordinal 400**

Actually, system properties have ordinal 400, so this source (500) will override them. Adjust the ordinal if different behavior is needed.

## Best Practices

1. **Use ConfigSourceUtil**: Always use `ConfigSourceUtil.activeWritableFileConfigSource()` to find the config source rather than creating new instances
2. **Check Optional**: Always check if the Optional is present before using the config source
3. **Error Handling**: Wrap file operations in try-catch blocks if needed
4. **Property Names**: Use consistent naming conventions (e.g., `module.component.setting`)
5. **Default Values**: Always provide defaults when reading properties that might not exist

## License

Part of the r-uu space-02 project.
