package de.ruu.lib.jpa.se.hibernate;

import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_URL;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_DRIVER;
import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.FORMAT_SQL;
import static org.hibernate.cfg.AvailableSettings.GENERATE_STATISTICS;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.QUERY_STARTUP_CHECKING;
import static org.hibernate.cfg.AvailableSettings.SHOW_SQL;
import static org.hibernate.cfg.AvailableSettings.STATEMENT_BATCH_SIZE;
import static org.hibernate.cfg.AvailableSettings.USE_QUERY_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_SECOND_LEVEL_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_STRUCTURED_CACHE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PersistenceUnitProperties
{
	public enum HBM2DLLAuto
	{
		NONE       ("none"),
		CREATE_ONLY("save-only"),
		DROP       ("drop"),
//		CREATE     ("save"),      // on startup performs drop, then save
		CREATE     ("create"),      // on startup performs drop, then save
//		CREATE_DROP("save-drop"), // on startup performs drop, then save, on shutdown performs drop
		CREATE_DROP("create-drop"), // on startup performs drop, then save, on shutdown performs drop
		VALIDATE   ("validate"),
		UPDATE     ("update"),
		;

		public final String value;

		HBM2DLLAuto(String value) { this.value = value; }
	}

	private Class<?>    dialect;
	private boolean     formatSQL;
	private boolean     generateStatistics;
	private HBM2DLLAuto hbm2ddlAuto;
	private Class<?>    jdbcDriver;
	private String      jdbcURL;
	private boolean     queryStartupChecking;
	private boolean     showSQL;
	private int         statementBatchSize;
	private boolean     useQueryCache;
	private boolean     useSecondLevelCache;
	private boolean     useStructuredCache;

	public static Builder builder() { return new Builder(); }

	public static class Builder
	{
		private final PersistenceUnitProperties instance = new PersistenceUnitProperties();

		public Builder dialect             (Class<?>    v) { instance.dialect              = v; return this; }
		public Builder formatSQL           (boolean     v) { instance.formatSQL            = v; return this; }
		public Builder generateStatistics  (boolean     v) { instance.generateStatistics   = v; return this; }
		public Builder hbm2ddlAuto         (HBM2DLLAuto v) { instance.hbm2ddlAuto         = v; return this; }
		public Builder jdbcDriver          (Class<?>    v) { instance.jdbcDriver           = v; return this; }
		public Builder jdbcURL             (String      v) { instance.jdbcURL              = v; return this; }
		public Builder queryStartupChecking(boolean     v) { instance.queryStartupChecking = v; return this; }
		public Builder showSQL             (boolean     v) { instance.showSQL              = v; return this; }
		public Builder statementBatchSize  (int         v) { instance.statementBatchSize   = v; return this; }
		public Builder useQueryCache       (boolean     v) { instance.useQueryCache        = v; return this; }
		public Builder useSecondLevelCache (boolean     v) { instance.useSecondLevelCache  = v; return this; }
		public Builder useStructuredCache  (boolean     v) { instance.useStructuredCache   = v; return this; }

		public PersistenceUnitProperties build() { return instance; }
	}

	/** @return with @code {@code {@link #formatSQL()} == true} and {@code {@link #showSQL()} == true} */
	public static PersistenceUnitProperties defaultProperties()
	{
		return
				PersistenceUnitProperties
						.builder()
								.formatSQL(true)
								.showSQL(  true)
						.build();
	}

	public Class<?>    dialect()              { return dialect; }
	public boolean     formatSQL()            { return formatSQL; }
	public boolean     generateStatistics()   { return generateStatistics; }
	public HBM2DLLAuto hbm2ddlAuto()         { return hbm2ddlAuto; }
	public Class<?>    jdbcDriver()           { return jdbcDriver; }
	public String      jdbcURL()              { return jdbcURL; }
	public boolean     queryStartupChecking() { return queryStartupChecking; }
	public boolean     showSQL()              { return showSQL; }
	public int         statementBatchSize()   { return statementBatchSize; }
	public boolean     useQueryCache()        { return useQueryCache; }
	public boolean     useSecondLevelCache()  { return useSecondLevelCache; }
	public boolean     useStructuredCache()   { return useStructuredCache; }

	public PersistenceUnitProperties dialect             (Class<?>    v) { this.dialect              = v; return this; }
	public PersistenceUnitProperties formatSQL           (boolean     v) { this.formatSQL            = v; return this; }
	public PersistenceUnitProperties generateStatistics  (boolean     v) { this.generateStatistics   = v; return this; }
	public PersistenceUnitProperties hbm2ddlAuto         (HBM2DLLAuto v) { this.hbm2ddlAuto         = v; return this; }
	public PersistenceUnitProperties jdbcDriver          (Class<?>    v) { this.jdbcDriver           = v; return this; }
	public PersistenceUnitProperties jdbcURL             (String      v) { this.jdbcURL              = v; return this; }
	public PersistenceUnitProperties queryStartupChecking(boolean     v) { this.queryStartupChecking = v; return this; }
	public PersistenceUnitProperties showSQL             (boolean     v) { this.showSQL              = v; return this; }
	public PersistenceUnitProperties statementBatchSize  (int         v) { this.statementBatchSize   = v; return this; }
	public PersistenceUnitProperties useQueryCache       (boolean     v) { this.useQueryCache        = v; return this; }
	public PersistenceUnitProperties useSecondLevelCache (boolean     v) { this.useSecondLevelCache  = v; return this; }
	public PersistenceUnitProperties useStructuredCache  (boolean     v) { this.useStructuredCache   = v; return this; }

	public Map<String, Object> properties()
	{
		Map<String, Object> result = new HashMap<>();

		result.put(FORMAT_SQL,               formatSQL());
		result.put(GENERATE_STATISTICS,      generateStatistics());
		result.put(JAKARTA_JDBC_URL,         jdbcURL());
		result.put(QUERY_STARTUP_CHECKING,   queryStartupChecking());
		result.put(SHOW_SQL,                 showSQL());
		result.put(USE_QUERY_CACHE,          useQueryCache());
		result.put(USE_SECOND_LEVEL_CACHE,   useSecondLevelCache());
		result.put(USE_STRUCTURED_CACHE,     useStructuredCache());
		result.put(STATEMENT_BATCH_SIZE,     statementBatchSize());

		if (dialect     != null) result.put(DIALECT            , dialect.getName());
		if (jdbcDriver  != null) result.put(JAKARTA_JDBC_DRIVER, jdbcDriver.getName());
		if (hbm2ddlAuto != null) result.put(HBM2DDL_AUTO       , hbm2ddlAuto.value);

		return Collections.unmodifiableMap(result);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof PersistenceUnitProperties other)) return false;
		return formatSQL            == other.formatSQL
			&& generateStatistics   == other.generateStatistics
			&& queryStartupChecking == other.queryStartupChecking
			&& showSQL              == other.showSQL
			&& statementBatchSize   == other.statementBatchSize
			&& useQueryCache        == other.useQueryCache
			&& useSecondLevelCache  == other.useSecondLevelCache
			&& useStructuredCache   == other.useStructuredCache
			&& Objects.equals(dialect,     other.dialect)
			&& Objects.equals(hbm2ddlAuto, other.hbm2ddlAuto)
			&& Objects.equals(jdbcDriver,  other.jdbcDriver)
			&& Objects.equals(jdbcURL,     other.jdbcURL);
	}

	@Override public int hashCode()
	{
		return Objects.hash(
			dialect, formatSQL, generateStatistics, hbm2ddlAuto, jdbcDriver, jdbcURL,
			queryStartupChecking, showSQL, statementBatchSize, useQueryCache, useSecondLevelCache, useStructuredCache);
	}

	@Override public String toString()
	{
		return "PersistenceUnitProperties(dialect=" + dialect
			+ ", formatSQL=" + formatSQL
			+ ", generateStatistics=" + generateStatistics
			+ ", hbm2ddlAuto=" + hbm2ddlAuto
			+ ", jdbcDriver=" + jdbcDriver
			+ ", jdbcURL=" + jdbcURL
			+ ", queryStartupChecking=" + queryStartupChecking
			+ ", showSQL=" + showSQL
			+ ", statementBatchSize=" + statementBatchSize
			+ ", useQueryCache=" + useQueryCache
			+ ", useSecondLevelCache=" + useSecondLevelCache
			+ ", useStructuredCache=" + useStructuredCache + ")";
	}
}
