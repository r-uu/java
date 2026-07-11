package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.Entity;
import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

/** Identifier equal expression */
public class IdentifierEqExpression<T> implements Criterion<T>
{
	private final Object value;

	protected IdentifierEqExpression(Object value)
	{
		this.value = value;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		criteriaQuery.setParam(value);
		return criteriaQuery.propertyName(Entity.P_ID, criteria) + " = ?";
	}
}