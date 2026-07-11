package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

public class SimpleExpression<T> implements Criterion<T>
{
	private final String property;
	private final Object value;
	private final String operator;

	protected SimpleExpression(String property, Object value, String operator)
	{
		this.property = property;
		this.value    = value;
		this.operator = operator;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		criteriaQuery.setParam(value);
		return criteriaQuery.propertyName(property, criteria) + operator + "?";
	}
}