package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

/** not expression */
public class NotExpression<T> implements Criterion<T>
{
	private final Criterion<T> criterion;

	protected NotExpression(Criterion<T> criterion)
	{
		this.criterion = criterion;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		return "not (" + criterion.toSqlString(criteria, criteriaQuery) + ')';
	}
}