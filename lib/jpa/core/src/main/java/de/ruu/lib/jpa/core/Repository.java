package de.ruu.lib.jpa.core;

import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Generic DAO interface for common data access functionality
 *
 * @param <ID> primary key's type
 * @param <T>  object's type, it must implement at least {@link Entity}
 *
 * @author r-uu
 */
public interface Repository<T extends Entity<ID>, ID extends Serializable>
{
	/** @return {@code Class<T>} of entity */
	Class<? extends Entity<ID>> entityClass();

	/** Retrieve a persisted object using the given id as primary key. */
	Optional<T> find(final ID id);

	/** Retrieve a persisted objects using the given ids as primary keys. */
	@NonNull Set<T> find(@NonNull Set<ID> ids);

	/** Retrieve all persisted objects. */
	@NonNull Set<T> findAll();

	/**
	 * Find using a named query.
	 * <p>
	 * Note that Named Queries are configured in the Entities and look like this:
	 *
	 * <pre>
	 *
	 * {@literal @}Entity2
	 * {@literal @}NamedQuery(name="findSalaryForNameAndDepartment",
	 *   query="SELECT e.salary " +
	 *         "FROM Employee e " +
	 *         "WHERE e.department.name = :deptName AND " +
	 *         "      e.name = :empName")
	 *  public class Employee {
	 *  ...
	 * </pre>
	 *
	 * @param queryName the name of the query
	 * @param params the query parameters
	 *
	 * @return the set of entities
	 */
	@NonNull Set<T> findByNamedQuery(final String queryName, Object... params);

	/** Count all entities. */
	long countAll();

	/** Find using a named query. */
	@NonNull Set<T> findByNamedQueryAndNamedParams(final String queryName, final Map<String, ?> params);

	/** @return {@code true} if {@code object} is not transient {@code and} is not managed and is not removed */
	boolean isDetached(final T object);

	/** Store brand-new entity. */
	@NonNull T create(T entity);

	/** Store brand-new entities. */
	@NonNull Set<T> create(@NonNull Set<T> entities);

	/** Update all changes made to an entity. */
	@NonNull T update(@NonNull T entity);

	/** Update all changes made to entities. */
	@NonNull Set<T> update(@NonNull Set<T> entities);

	/** Remove an entity by given id. */
	boolean delete(@NonNull ID id);

	/** Remove an entity. */
	boolean delete(@NonNull T entity);

	/** Refresh an entity that may have changed in another thread/transaction. */
	void refresh(@NonNull T entity);

	/** Write to database anything that is pending operation and clear it. */
	void flushAndClear();
}