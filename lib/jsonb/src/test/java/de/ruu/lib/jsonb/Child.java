package de.ruu.lib.jsonb;

/**
 * data class for Jsonb tests
 * <p>
 * data classes have to be top-level, {@code public} and equipped with a {@code public} no-args default constructor
 *
 * @author ruu
 */
public class Child
{
	private String field;

	/** has to be {@code public} for Jsonb */
	public Child() {}
	Child(String field) { this.field = field; }

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder(getClass().getSimpleName() + " [field=" + field + "]");
		return result.toString();
	}

	String getField() { return field; }
}