package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

/** Logical expression */
public class LogicalExpression<T> implements Criterion<T>
{

	private final Criterion<T> lhs;
	private final Criterion<T> rhs;
	private final String    operator;

	protected LogicalExpression(Criterion<T> lhs, Criterion<T> rhs, String operator)
	{
		this.lhs      = lhs;
		this.rhs      = rhs;
		this.operator = operator;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		return
			  "(" + lhs.toSqlString(criteria, criteriaQuery) + " " + operator + " "
	          + rhs.toSqlString(criteria, criteriaQuery)
				+ ")";
	}
}