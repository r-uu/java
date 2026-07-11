package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

/** Insensitive In expression */
public class InExpressionInsensitive<T> implements Criterion<T>
{
	private final String   property;
	private final String[] values;

	protected InExpressionInsensitive(String property, String[] values)
	{
		this.property = property;
		this.values   = values;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		String sql = "lower(" + criteriaQuery.propertyName(property, criteria) + ") in (";

		for (String v : values)
		{
			criteriaQuery.setParam(v.toLowerCase());
			sql += "?, ";
		}

		return sql.substring(0, sql.length() - 2) + ")";
	}
}