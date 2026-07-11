package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

public class BetweenExpression<T> implements Criterion<T>
{
	private final String property;
	private final Object lo;
	private final Object hi;

	protected BetweenExpression(String property, Object lo, Object hi)
	{
		this.property = property;
		this.lo       = lo;
		this.hi       = hi;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		criteriaQuery.setParam(lo);
		criteriaQuery.setParam(hi);
		return criteriaQuery.propertyName(property, criteria) + " between ? and ?";
	}
}