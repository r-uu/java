package de.ruu.lib.jpa.core.criteria.restriction;

/** Empty expression */
public class EmptyExpression<T> extends AbstractEmptynessExpression<T>
{
	protected EmptyExpression(String property)
	{
		super(property);
	}

	@Override protected boolean excludeEmpty()
	{
		return false;
	}
}