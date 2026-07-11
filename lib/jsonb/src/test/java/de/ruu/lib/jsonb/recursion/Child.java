package de.ruu.lib.jsonb.recursion;

import jakarta.json.bind.annotation.JsonbTransient;

import java.util.Objects;

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

	@JsonbTransient
	private Parent parent;

	public Child() {}

	public Child(String field, Parent parent)
	{
		this.field  = Objects.requireNonNull(field,  "field");
		this.parent = Objects.requireNonNull(parent, "parent");

		parent.getChildren().add(this);
	}

	public String getField()  { return field; }
	public Parent getParent() { return parent; }

	@Override public String toString()
	{
		return "Child [field=" + field + "]";
	}
}
