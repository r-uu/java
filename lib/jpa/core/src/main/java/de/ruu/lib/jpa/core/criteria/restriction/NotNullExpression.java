package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

/** not null expression */
public class NotNullExpression<T> implements Criterion<T>
{
	private final String property;

	protected NotNullExpression(String property)
	{
		this.property = property;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		return criteriaQuery.propertyName(property, criteria) + " is not null";
	}
}