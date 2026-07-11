package de.ruu.lib.jpa.core.criteria;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Criteria used to build EQL queries. */
public class Criteria<T>
{
	// Helper method to get the actual type parameter at runtime
	protected Class<T> type()
	{
		if (this instanceof Subcriteria)
		{
			return ((Subcriteria<T>) this).parentCriteria().type();
		}
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) Object.class;
		return type;
	}

	/**
	 * Join types which can be used to <code>addAlias</code> and <code>createCriteria</code> methods.
	 */
	public enum JoinType
	{
		/** Inner join. Default one. */
		INNER_JOIN("inner join"),
		/** Left outer join. */
		LEFT_JOIN("left outer join");

		private final String sql;
		JoinType(String sql) { this.sql = sql;}
		public String toSqlString() { return sql; }
	}

	private final String      name;
	private final String      alias;
	private final Criteria<T> parent;
	private final JoinType    joinType;

	private final List<CriterionEntry<T>> criterionList   = new ArrayList<>();
	private final List<OrderEntry<T>>     orderList       = new ArrayList<>();
	private final List<Subcriteria<T>>    subcriteriaList = new ArrayList<>();

	private int           aliasNumber;
	private Projection<T> projection;
	private Criteria<T>                   projectionCriteria;
	private Integer maxResults;
	private Integer firstResult;

	/** Create new criteria for specified <code>IEntity</code> implementation. */
    public static <T> Criteria<T> forClass(Class<T> entity) {
        return new Criteria<>(getEntityName(entity), "this", null, null) {
            @Override
            protected Class<T> type() {
                return entity;
            }
        };
    }

	private Criteria(String name, String alias, JoinType joinType, Criteria<T> parent)
	{
		this.name = name;
		this.alias = alias;
		this.parent = parent;
		this.joinType = joinType;
	}

	protected String name() { return name; }
	protected Criteria<T> parent() { return parent; }
	protected String alias() { return alias; }

	/**
	 * Get join type.
	 *
	 * @return join type
	 */
	protected JoinType joinType()
	{
		return joinType;
	}

	/**
	 * Specify that the query results will be a projection. The individual components contained within the given
	 * <code>Projection</code> determines the overall "shape" of the query result.
	 *
	 * @param projection projection used in query
	 * @return criteria object
	 * @see Projection
	 */
	public Criteria<T> setProjection(Projection<T> projection)
	{
		this.projection = projection;
		this.projectionCriteria = this;
		return this;
	}

	/** Add a <code>Criterion</code> to constrain the results to be retrieved. */
	public Criteria<T> add(Criterion<T> criterion)
	{
		addCriterion(criterion);
		return this;
	}

	/** Add an <code>Order</code> to the result set. */
	public Criteria<T> addOrder(Order<T> order)
	{
		addOrderEntry(order);
		return this;
	}

	// interne Helfer zum Hinzufügen ohne die privaten Entry-Typen nach außen zu leaken
	protected void addCriterion(Criterion<T> criterion) { criterionList.add(new CriterionEntry<>(criterion, this)); }
	protected void addOrderEntry(Order<T> order)        { orderList   .add(new OrderEntry<>(order    , this)); }

	/**
	 * Create a new <code>Criteria</code> joined using "inner join".
	 *
	 * @param name criteria entity name
	 * @return subcriteria
	 */
	public Criteria<T> createCriteria(String name)
	{
		return new Subcriteria<>(name, createAlias(name), JoinType.INNER_JOIN, this);
	}

	/**
	 * Create a new <code>Criteria</code>.
	 *
	 * @param name     criteria entity name
	 * @param joinType join type
	 * @return subcriteria
	 */
	public Criteria<T> createCriteria(String name, JoinType joinType)
	{
		return new Subcriteria<>(name, createAlias(name), joinType, this);
	}

	/**
	 * Create a new alias joined using "inner join".
	 *
	 * @param name  criteria entity name
	 * @param alias alias
	 * @return criteria
	 */
	public Criteria<T> createAlias(String name, String alias)
	{
		new Subcriteria<>(name, alias, JoinType.INNER_JOIN, this);
		return this;
	}

	/**
	 * Create a new alias.
	 *
	 * @param name     criteria entity name
	 * @param alias    alias
	 * @param joinType join type
	 * @return criteria
	 */
	public Criteria<T> createAlias(String name, String alias, JoinType joinType)
	{
		new Subcriteria<>(name, alias, joinType, this);
		return this;
	}

	/**
	 * Set a limit upon the number of objects to be retrieved.
	 *
	 * @param maxResults number of objects to be retrieved
	 * @return criteria object
	 */
	public Criteria<T> setMaxResults(int maxResults)
	{
		this.maxResults = maxResults;
		return this;
	}

	/**
	 * Set the first result to be retrieved.
	 *
	 * @param firstResult first result to be retrieved
	 * @return criteria object
	 */
	public Criteria<T> setFirstResult(int firstResult)
	{
		this.firstResult = firstResult;
		return this;
	}

	/** Returns all results for this criteria. */
	public List<T> list(EntityManager entityManager)
	{
		return prepareQuery(entityManager).getResultList();
	}

	/** Returns exactly one result or throws NoResultException/NonUniqueResultException. */
	public T uniqueResult(EntityManager entityManager) throws NonUniqueResultException, NoResultException
	{
		List<T> results = list(entityManager);
		if (results.isEmpty()) throw new NoResultException("No results found");
		if (results.size() > 1) throw new NonUniqueResultException("More than one result found: " + results.size());
		return results.get(0);
	}

	@Override
	public String toString()
	{
		CriteriaQuery criteriaQuery = new CriteriaQuery();

		String result = prepateEql(criteriaQuery);

		if (!criteriaQuery.params().isEmpty())
		{
			result += " [" + criteriaQuery.params() + "]";
		}

		return result;
	}

	protected final String createAlias(String name)
	{
		return name.replace('.', '_') + "_" + aliasNumber++;
	}

	private String prepateEql(CriteriaQuery criteriaQuery)
	{
		String sql = "from " + name + " as " + alias + " ";
		criteriaQuery.registerAlias(alias);

		for (Criteria<T> subcriteria : subcriteriaList)
		{
			sql += subcriteria.joinType().toSqlString() + " ";
			sql += criteriaQuery.propertyName(subcriteria.name(), subcriteria.parent());
			sql += " as " + subcriteria.alias() + " ";
			criteriaQuery.registerAlias(subcriteria.alias());
		}

		if (projection != null)
		{
			String projectionSql = projection.toSqlString(projectionCriteria, criteriaQuery);
			if (!projectionSql.isEmpty())
			{
				sql = "select " + projectionSql + " " + sql;
			}
			else
			{
				sql = "select this " + sql;
			}
		}
		else
		{
			sql = "select this " + sql;
		}

		String criterionSql = "";

		for (CriterionEntry<T> criterionEntry : criterionList)
		{
			if (!criterionSql.isEmpty())
			{
				criterionSql += " and ";
			}
			criterionSql += criterionEntry.criterion().toSqlString(criterionEntry.criteria(), criteriaQuery);
		}

		if (!criterionSql.isEmpty())
		{
			sql += "where " + criterionSql + " ";
		}

		if (projection != null)
		{
			if (projection.grouped())
			{
				String groupBySql = projection.toGroupSqlString(projectionCriteria, criteriaQuery);
				if (!groupBySql.isEmpty())
				{
					sql += "group by " + groupBySql + " ";
				}
			}
		}

		String orderSql = "";

		for (OrderEntry<T> orderEntry : orderList)
		{
			if (!orderSql.isEmpty())
			{
				orderSql += ",";
			}
			orderSql += orderEntry.order().toSqlString(orderEntry.criteria(), criteriaQuery);
		}

		if (!orderSql.isEmpty())
		{
			sql += "order by " + orderSql + " ";
		}

		return sql.trim();
	}

	private TypedQuery<T> prepareQuery(EntityManager entityManager)
	{
		CriteriaQuery criteriaQuery = new CriteriaQuery();

		String sql = prepateEql(criteriaQuery);

		TypedQuery<T> query = entityManager.createQuery(sql, type());

		if (firstResult != null)
		{
			query.setFirstResult(firstResult);
		}

		if (maxResults != null)
		{
			query.setMaxResults(maxResults);
		}

		int i = 1;

		for (Object property : criteriaQuery.params())
		{
			query.setParameter(i++, property);
		}

		return query;
	}

	private static <T> String getEntityName(Class<T> entity)
	{
		Entity entityAnnotation = entity.getAnnotation(Entity.class);
		if (entityAnnotation != null && !entityAnnotation.name().isEmpty()) {
			return entityAnnotation.name();
		}
		// Fall back to simple class name if annotation name is empty or missing
		return entity.getSimpleName();
	}

	/**
	 * Information about current query, for example parameters.
	 */
	public final class CriteriaQuery
	{

		private final List<Object> params = new ArrayList<>();

		private final Set<String> aliases = new HashSet<>();

		CriteriaQuery()
		{
		}

		/**
		 * Get name of property in given criteria context.
		 *
		 * @param name     property's name
		 * @param criteria criteria
		 * @return proper name which can be used in EQL
		 */
		public String propertyName(String name, Criteria<T> criteria)
		{
			int pos = name.indexOf(".");

			if (pos == -1)
			{
				return criteria.alias() + "." + name;
			}
			else
			{
				if (aliases.contains(name.substring(0, pos)))
				{
					return name;
				}
				else
				{
					return criteria.alias() + "." + name;
				}
			}
		}

		/** Set query's param. */
		public void setParam(Object param)
		{
			params.add(param);
		}

		/** Register alias. */
		void registerAlias(String alias)
		{
			this.aliases.add(alias);
		}

		/** Get all query's params. */
		List<Object> params()
		{
			return params;
		}
	}

	/**
	 * Subcritera associated with root criteria.
	 */
	public static final class Subcriteria<T> extends Criteria<T> {
    private final Criteria<T> parentCriteria;

    Subcriteria(String name, String alias, JoinType joinType, Criteria<T> parent) {
        super(name, alias, joinType, parent);
        this.parentCriteria = parent;
        parent.subcriteriaList.add(this);
    }
    
    Criteria<T> parentCriteria() {
        return parentCriteria;
    }

    @Override
    public Criteria<T> add(Criterion<T> criterion) {
        this.addCriterion(criterion);
        return this;
    }

    @Override
    public Criteria<T> addOrder(Order<T> order) {
        this.addOrderEntry(order);
        return this;
    }

    @Override
    public Criteria<T> createCriteria(String name) {
        return new Subcriteria<>(name, createAlias(name), JoinType.INNER_JOIN, this);
    }

    @Override
    public Criteria<T> createCriteria(String name, JoinType joinType) {
        return new Subcriteria<>(name, createAlias(name), joinType, this);
    }

    @Override
    public Criteria<T> createAlias(String name, String alias) {
        new Subcriteria<>(name, alias, JoinType.INNER_JOIN, this);
        return this;
    }

    @Override
    public Criteria<T> createAlias(String name, String alias, JoinType joinType) {
        new Subcriteria<>(name, alias, joinType, this);
        return this;
    }

    @Override
    public List<T> list(EntityManager entityManager) {
        return parentCriteria.list(entityManager);
    }

    @Override
    public T uniqueResult(EntityManager entityManager) throws NonUniqueResultException, NoResultException {
        return parentCriteria.uniqueResult(entityManager);
    }

    @Override
    public Criteria<T> setFirstResult(int firstResult) {
        parentCriteria.setFirstResult(firstResult);
        return this;
    }

    @Override
    public Criteria<T> setMaxResults(int maxResults) {
        parentCriteria.setMaxResults(maxResults);
        return this;
    }

    @Override
    public Criteria<T> setProjection(Projection<T> projection) {
        parentCriteria.setProjection(projection);
        return this;
    }
	}

	private static final class CriterionEntry<T>
	{
		private final Criterion<T> criterion;
		private final Criteria<T> criteria;

		CriterionEntry(Criterion<T> criterion, Criteria<T> criteria)
		{
			this.criteria = criteria;
			this.criterion = criterion;
		}

		private Criterion<T> criterion()
		{
			return criterion;
		}

		private Criteria<T> criteria()
		{
			return criteria;
		}
	}

	private static final class OrderEntry<T>
	{
		private final Order<T> order;
		private final Criteria<T> criteria;

		OrderEntry(Order<T> order, Criteria<T> criteria)
		{
			this.criteria = criteria;
			this.order = order;
		}

		private Order<T> order()
		{
			return order;
		}

		private Criteria<T> criteria()
		{
			return criteria;
		}
	}
}
