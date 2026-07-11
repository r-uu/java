package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

public abstract class AbstractEmptynessExpression<T> implements Criterion<T>
{
	protected final String property;

	protected AbstractEmptynessExpression(String property)
	{
		this.property = property;
	}

	/** @return {@code true} if exclude is empty */
	protected abstract boolean excludeEmpty();

	@Override public final String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		return
			  (excludeEmpty()
			? "exists"
			: "not exists") + " (select 1 from " + criteriaQuery.propertyName(property, criteria) + ")";
	}
}