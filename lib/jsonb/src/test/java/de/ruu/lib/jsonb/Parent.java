package de.ruu.lib.jsonb;

import java.util.HashSet;
import java.util.Set;

/**
 * data class for Jsonb tests
 * <p>
 * data classes have to be top-level, {@code public} and equipped with a {@code public} no-args default constructor
 *
 * @author ruu
 */
public class Parent
{
	private String field;
	private Set<Child> children = new HashSet<>();

	/** has to be {@code public} for Jsonb */
	public Parent() {}
	Parent(String field) { this.field = field; }

	public String getField() { return field; }
	public Set<Child> getChildren() { return children; }

	@Override public String toString()
	{
		StringBuilder result = new StringBuilder(getClass().getSimpleName() + " [field=" + field + "]");
		children.forEach(child -> result.append("\n\t" + child));
		return result.toString();
	}
}