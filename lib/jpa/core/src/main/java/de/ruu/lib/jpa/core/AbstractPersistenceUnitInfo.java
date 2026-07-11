package de.ruu.lib.jpa.core;

import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static jakarta.persistence.PersistenceUnitTransactionType.RESOURCE_LOCAL;
import static jakarta.persistence.SharedCacheMode.UNSPECIFIED;
import static jakarta.persistence.ValidationMode.AUTO;
import static java.lang.Thread.currentThread;

/** {@link #transactionType} is #RESOURCE_LOCAL by default */
@SuppressWarnings({"removal"})
public abstract class AbstractPersistenceUnitInfo implements PersistenceUnitInfo
{
	public static final String JPA_VERSION = "3.2";

	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_UNIT_NAME =
			"persistence.unit.persistence.unit.name";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_PROVIDER_CLASS_NAME =
			"persistence.unit.persistence.provider.class.name";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_TRANSACTION_TYPE =
			"persistence.unit.transaction.type";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_JTA_DATA_SOURCE =
			"persistence.unit.jta.data.source";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_NON_JTA_DATA_SOURCE =
			"persistence.unit.non.jta.data.source";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_MAPPING_FILE_NAMES =
			"persistence.unit.mapping.file.names";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_JAR_FILE_URLS =
			"persistence.unit.jar.file.urls";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_UNIT_ROOT_URL =
			"persistence.unit.persistence.unit.root.url";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_MANAGED_CLASS_NAMES =
			"persistence.unit.managed.class.names";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_EXCLUDE_UNLISTED_CLASSES =
			"persistence.unit.exclude.unlisted.classes";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_SHARED_CACHE_MODE =
			"persistence.unit.shared.cache.mode";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_VALIDATION_MODE =
			"persistence.unit.validation.mode";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_PROPERTIES =
			"persistence.unit.properties";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_XML_SCHEMA_VERSION =
			"persistence.unit.persistence.xml.schema.version";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_CLASS_LOADER =
			"persistence.unit.class.loader";
	public static final String PROPERTY_KEY_PERSISTENCE_UNIT_NEW_TEMP_CLASS_LOADER =
			"persistence.unit.new.temp.class.loader";

	private final String     persistenceUnitName;
	private final Class<?>   persistenceProvider;
	private final DataSource dataSource;

	@NonNull private List<String>                   mappingFileNames  = new ArrayList<>();
	@NonNull private List<URL>                      jarFileUrls       = new ArrayList<>();
	@NonNull private List<String>                   managedClassNames = new ArrayList<>();
	@NonNull private Properties                     properties        = new Properties();
	@NonNull private List<ClassTransformer>         classTransformers = new ArrayList<>();
	@NonNull private PersistenceUnitTransactionType transactionType   = RESOURCE_LOCAL;

	private URL             persistenceUnitRootUrl;
	private Boolean         excludeUnlistedClasses      = false;
	private SharedCacheMode sharedCacheMode             = UNSPECIFIED;
	private ValidationMode  validationMode              = AUTO;
	private String          persistenceXMLSchemaVersion = JPA_VERSION;
	private ClassLoader     newTempClassLoader;
	private ClassLoader     classLoader                 = currentThread().getContextClassLoader();

	protected AbstractPersistenceUnitInfo(
			String     persistenceUnitName,
			Class<?>   persistenceProvider,
			DataSource dataSource)
	{
		this.persistenceUnitName = persistenceUnitName;
		this.persistenceProvider = persistenceProvider;
		this.dataSource          = dataSource;
	}

	// --- getters ---
	public String                   getPersistenceUnitName()      { return persistenceUnitName; }
	public List<String>             getMappingFileNames()         { return mappingFileNames; }
	public List<URL>                getJarFileUrls()              { return jarFileUrls; }
	public URL                      getPersistenceUnitRootUrl()   { return persistenceUnitRootUrl; }
	public List<String>             getManagedClassNames()        { return managedClassNames; }
	public SharedCacheMode          getSharedCacheMode()          { return sharedCacheMode; }
	public ValidationMode           getValidationMode()           { return validationMode; }
	public String                   getPersistenceXMLSchemaVersion() { return persistenceXMLSchemaVersion; }
	public ClassLoader              getClassLoader()              { return classLoader; }
	public ClassLoader              getNewTempClassLoader()       { return newTempClassLoader; }
	public List<ClassTransformer>   classTransformers()           { return classTransformers; }
	public PersistenceUnitTransactionType transactionType()       { return transactionType; }

	// --- setters ---
	public AbstractPersistenceUnitInfo mappingFileNames  (@NonNull List<String>                   v) { this.mappingFileNames           = v; return this; }
	public AbstractPersistenceUnitInfo jarFileUrls       (@NonNull List<URL>                      v) { this.jarFileUrls                = v; return this; }
	public AbstractPersistenceUnitInfo managedClassNames (@NonNull List<String>                   v) { this.managedClassNames          = v; return this; }
	public AbstractPersistenceUnitInfo properties        (@NonNull Properties                     v) { this.properties                 = v; return this; }
	public AbstractPersistenceUnitInfo classTransformers (@NonNull List<ClassTransformer>         v) { this.classTransformers          = v; return this; }
	public AbstractPersistenceUnitInfo transactionType   (@NonNull PersistenceUnitTransactionType v) { this.transactionType            = v; return this; }
	public AbstractPersistenceUnitInfo persistenceUnitRootUrl     (URL             v) { this.persistenceUnitRootUrl     = v; return this; }
	public AbstractPersistenceUnitInfo excludeUnlistedClasses     (Boolean         v) { this.excludeUnlistedClasses     = v; return this; }
	public AbstractPersistenceUnitInfo sharedCacheMode            (SharedCacheMode v) { this.sharedCacheMode            = v; return this; }
	public AbstractPersistenceUnitInfo validationMode             (ValidationMode  v) { this.validationMode             = v; return this; }
	public AbstractPersistenceUnitInfo persistenceXMLSchemaVersion(String          v) { this.persistenceXMLSchemaVersion = v; return this; }
	public AbstractPersistenceUnitInfo newTempClassLoader         (ClassLoader     v) { this.newTempClassLoader         = v; return this; }
	public AbstractPersistenceUnitInfo classLoader                (ClassLoader     v) { this.classLoader                = v; return this; }

	public Class<?> getPersistenceProvider()                  { return persistenceProvider; }
	@Override public String getPersistenceProviderClassName() { return persistenceProvider.getName(); }

	// TODO: find out if it makes sense to maintain only one datasource and return this for jta and
	//       resource local transaction mode
	@Override public DataSource getJtaDataSource()    { return dataSource; }
	@Override public DataSource getNonJtaDataSource() { return dataSource; }

	@Override public void addTransformer(ClassTransformer transformer)
	{
		classTransformers.add(transformer);
	}

	@Override public boolean excludeUnlistedClasses() { return excludeUnlistedClasses; }

	@Override public Properties getProperties()
	{
		Properties result = new Properties();

		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_UNIT_NAME,           persistenceUnitName);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_TRANSACTION_TYPE,                transactionType);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_MAPPING_FILE_NAMES,              mappingFileNames);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_JAR_FILE_URLS,                   jarFileUrls);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_MANAGED_CLASS_NAMES,             managedClassNames);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_EXCLUDE_UNLISTED_CLASSES,        excludeUnlistedClasses);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_SHARED_CACHE_MODE,               sharedCacheMode);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_VALIDATION_MODE,                 validationMode);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_PROPERTIES,                      properties);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_XML_SCHEMA_VERSION,  persistenceXMLSchemaVersion);
		result.put(PROPERTY_KEY_PERSISTENCE_UNIT_CLASS_LOADER,                    classLoader);

		if (persistenceProvider    != null)
			result.put(
					PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_PROVIDER_CLASS_NAME,
					persistenceProvider.getName());
		if (dataSource             != null)
			result.put(PROPERTY_KEY_PERSISTENCE_UNIT_JTA_DATA_SOURCE,           dataSource);
		if (persistenceUnitRootUrl != null)
			result.put(PROPERTY_KEY_PERSISTENCE_UNIT_PERSISTENCE_UNIT_ROOT_URL, persistenceUnitRootUrl);
		if (newTempClassLoader     != null)
			result.put(PROPERTY_KEY_PERSISTENCE_UNIT_NEW_TEMP_CLASS_LOADER,     newTempClassLoader);

		return result;
	}

	/** Compatibility with current SPI: convert API enum to SPI enum on return */
	@Override
	@SuppressWarnings({"removal"})
	public jakarta.persistence.spi.PersistenceUnitTransactionType getTransactionType()
	{
		return jakarta.persistence.spi.PersistenceUnitTransactionType.valueOf(transactionType.name());
	}

	public Properties properties() { return getProperties(); }

	@SuppressWarnings("unused")
	public static List<String> toClassNameList(Set<Class<?>> managedClasses)
	{
		List<String> result = new ArrayList<>();
		managedClasses.forEach(c -> result.add(c.getName()));
		return result;
	}

	public void addManagedClass(Class<?> clazz)
	{
		managedClassNames.add(clazz.getName());
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof AbstractPersistenceUnitInfo other)) return false;
		return Objects.equals(persistenceUnitName, other.persistenceUnitName);
	}

	@Override public int hashCode() { return Objects.hash(persistenceUnitName); }

	@Override public String toString()
	{
		return getClass().getSimpleName() + "(persistenceUnitName=" + persistenceUnitName + ")";
	}
}
