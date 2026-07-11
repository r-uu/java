package de.ruu.lib.jpa.core.criteria.restriction;

/** Disjunction (OR) */
public class Disjunction<T> extends Junction<T>
{
	protected Disjunction()
	{
		super("or");
	}
}