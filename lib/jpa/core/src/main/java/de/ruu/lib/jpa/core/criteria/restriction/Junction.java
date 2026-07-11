package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criteria;
import de.ruu.lib.jpa.core.criteria.Criterion;

import java.util.ArrayList;
import java.util.List;

/** Junction expression */
public class Junction<T> implements Criterion<T>
{
	private final List<Criterion<T>> criteria = new ArrayList<>();
	private final String          operator;

	protected Junction(String operator)
	{
		this.operator = operator;
	}

	/**
	 * Add criterion to junction.
	 *
	 * @param criterion criterion
	 * @return junction
	 */
	public Junction<T> add(Criterion<T> criterion)
	{
		criteria.add(criterion);
		return this;
	}

	@Override public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
	{
		if (this.criteria.isEmpty()) return "1=1";

		StringBuilder result = new StringBuilder("(");

		for (Criterion<T> criterion : this.criteria)
		{
			if (result.length() > 1)
			{
				result.append(" ").append(operator).append(" ");
			}
			result.append(criterion.toSqlString(criteria, criteriaQuery));
		}

		return result.append(")").toString();
	}
}