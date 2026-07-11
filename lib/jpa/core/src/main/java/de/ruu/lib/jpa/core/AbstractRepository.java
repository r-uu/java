package de.ruu.lib.jpa.core;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.restriction.Restrictions;
import de.ruu.lib.util.Reflection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Abstract implementation of generic DAO.
 * <p>
 * Note: It is intentional that there is no automatic transaction handling here.
 *
 * @param <ID> entity's primary key, it has to be {@link Serializable}
 * @param <T>  entity type, it implements at least {@code Entity<ID>}
 * @see Entity
 *
 * @author r-uu
 */
public abstract class AbstractRepository<T extends Entity<ID>, ID extends Serializable>
		implements Repository<T, ID>, AutoCloseable
{
	private final Class<T> clazz;

	/**
	 * Determines value for {@link #clazz} from type arguments of implementing class by reflection. Also works for proxied
	 * instances in e.g. CDI environments.
	 */
	@SuppressWarnings(value = "unchecked")
	protected AbstractRepository()
	{
		Optional<ParameterizedType> optional = Reflection.firstParameterizedTypeInParents(getClass());

		if (optional.isPresent())
		{
			ParameterizedType parameterizedTypeForAbstractDAO = optional.get();

			Type type = parameterizedTypeForAbstractDAO.getActualTypeArguments()[0];

			try
			{
				clazz = (Class<T>) Class.forName(type.getTypeName());
			}
			catch (ClassNotFoundException e)
			{
				throw new IllegalStateException("could not find class " + type.getTypeName(), e);
			}
		}
		else
		{
			throw new IllegalStateException("could not lookup parameterized type in parents");
		}
	}

	protected abstract @NonNull EntityManager entityManager();

	@Override public Class<T> entityClass() { return clazz; }

	@Override public Optional<T>     find   (@NonNull     ID  id ) { return Optional.ofNullable(entityManager().find(clazz, id)); }
	@Override public @NonNull Set<T> findAll(                    ) { return findByCriteria(Criteria.forClass(clazz)); }
	@Override public @NonNull Set<T> find   (@NonNull Set<ID> ids) { return findByCriteria(Criteria.forClass(clazz).add(Restrictions.in(Entity.P_ID, ids))); }

	@Override public @NonNull Set<T> findByNamedQuery(final String name, Object... params)
	{
		TypedQuery<T> query = entityManager().createNamedQuery(name, clazz);
		for (int i = 0; i < params.length; i++) { query.setParameter(i + 1, params[i]); }
		return new HashSet<>(query.getResultList());
	}

	@Override public @NonNull Set<T> findByNamedQueryAndNamedParams(final String name, final Map<String, ?> params)
	{
		TypedQuery<T> query = entityManager().createNamedQuery(name, clazz);
		params.forEach(query::setParameter);
		return new HashSet<>(query.getResultList());
	}

	@Override public long countAll()
	{
		return
				entityManager()
					.createQuery(
							"select count(*) from " + entityManager().getMetamodel().entity(clazz).getName(),
							Long.class)
					.getSingleResult();
	}

	@Override public boolean isDetached(final T object)
	{
		if (object.optionalId().isPresent())           // must not be transient
		{
			if (entityManager().contains(object) == false) // must not be managed now
			{
				if (find(object.getId()).isPresent())          // must not be removed
				{
					return true;
				}
			}
		}
		return false;
	}

	/** Inserts a new entity into the database. This method should only be used for brand-new entities. */
	@Override public @NonNull T create(@NonNull T entity)
	{
		entityManager().persist(entity);
		return entity;
	}

	@Override public @NonNull Set<T> create(@NonNull Set<T> entities)
	{
		entities.forEach(e -> entityManager().persist(e));
		return entities;
	}

	/** Inserts multiple new entities into the database. All entities must be brand-new. */
	public Set<T> createAll(@NonNull Set<T> entities)
	{
		entities.forEach(e -> entityManager().persist(e));
		return entities;
	}

	/**
	 * Updates an existing entity.
	 * <p>
	 * Trusts JPA merge() semantics: managed entities are returned as-is, detached entities are
	 * merged back into the persistence context. A new entity (no id) will be inserted by merge().
	 */
	@Override public @NonNull T update(@NonNull T entity)
	{
		if (entityManager().contains(entity)) return entity;
		return entityManager().merge(entity);
	}

	@Override public @NonNull Set<T> update(@NonNull Set<T> entities)
	{
		Set<T> result = new HashSet<>();
		entities.forEach(e -> result.add(update(e)));
		return result;
	}

	@Override public boolean delete(final @NonNull ID id)
	{
		Optional<T> optional = find(id);

		if (optional.isPresent())
		{
			delete(optional.get());
			return true;
		}

		return false;
	}

	@Override public boolean delete(@NonNull T entity)
	{
		entityManager().remove(entity);
		return true;
	}

	@Override public void refresh(final @NonNull T entity)
	{
		entityManager().refresh(entity);
	}

	@Override public void flushAndClear()
	{
		entityManager().flush();
		entityManager().clear();
	}

	/**
	 * Retrieve objects using criteria. It is equivalent to <code>criteria.list(entityManager)</code>.
	 *
	 * @param criteria criteria which will be executed
	 * @return set of found objects
	 * @see jakarta.persistence.TypedQuery#getResultList()
	 */
	protected Set<T> findByCriteria(Criteria<T> criteria)
	{
		List<T> list = criteria.list(entityManager());
		return new HashSet<>(list);
	}

	/**
	 * Retrieve an unique object using criteria. It is equivalent to
	 * <code>criteria.uniqueResult(entityManager)</code>.
	 *
	 * @param criteria criteria which will be executed
	 * @return retrieved object
	 * @throws NoResultException - if there is no result
	 * @throws NonUniqueResultException - if more than one result
	 * @see jakarta.persistence.TypedQuery#getSingleResult()
	 */
	protected Object findUniqueByCriteria(Criteria<T> criteria)
			throws NonUniqueResultException, NoResultException { return criteria.uniqueResult(entityManager()); }

	@Override public void close()
	{
		EntityManager entityManager = entityManager();

		if (entityManager.isOpen())
		{
			entityManager.getTransaction().begin();
			entityManager.flush();
			entityManager.getTransaction().commit();
			entityManager.close();
		}
	}
}