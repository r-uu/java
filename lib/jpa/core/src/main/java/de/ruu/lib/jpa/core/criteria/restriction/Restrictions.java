package de.ruu.lib.jpa.core.criteria.restriction;

import de.ruu.lib.jpa.core.criteria.Criterion;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/** restrictions builder */
public class Restrictions
{
	Restrictions() { }

	public static <T> Criterion<T> idEq(Object value)
	{
		return new IdentifierEqExpression<T>(value);
	}
	public static <T> Criterion<T> eq(String property, Object value)
	{
		return new SimpleExpression<T>(property, value, "=");
	}
	public static <T> Criterion<T> ne(String property, Object value)
	{
		return new SimpleExpression<T>(property, value, "<>");
	}
	public static <T> Criterion<T> like(String property, Object value)
	{
		return new LikeExpression<T>(property, value, null, false);
	}
	public static <T> Criterion<T> ilike(String property, Object value)
	{
		return new LikeExpression<T>(property, value, null, true);
	}
	public static <T> Criterion<T> gt(String property, Object value)
	{
		return new SimpleExpression<T>(property, value, ">");
	}
	public static <T> Criterion<T> lt(String property, Object value)
	{
		return new SimpleExpression<T>(property, value, "<");
	}
	public static <T> Criterion<T> le(String property, Object value)
	{
		return new SimpleExpression<T>(property, value, "<=");
	}
	public static <T> Criterion<T> ge(String property, Object value)
	{
		return new SimpleExpression<T>(property, value, ">=");
	}
	public static <T> Criterion<T> between(String property, Object lo, Object hi)
	{
		return new BetweenExpression<T>(property, lo, hi);
	}
	public static <T> Criterion<T> in(String property, Object[] values)
	{
		return new InExpression<T>(property, values);
	}
	public static <T> Criterion<T> in(String property, Collection<?> values)
	{
		return new InExpression<T>(property, values.toArray());
	}
	public static <T> Criterion<T> iin(String property, String[] values)
	{
		return new InExpressionInsensitive<T>(property, values);
	}
	public static <T> Criterion<T> iin(String property, Collection<String> values)
	{
		return new InExpressionInsensitive<T>(property, values.toArray(new String[values.size()]));
	}
	public static <T> Criterion<T> isNull(String property)
	{
		return new NullExpression<T>(property);
	}
	public static <T> Criterion<T> eqProperty(String property, String otherProperty)
	{
		return new PropertyExpression<T>(property, otherProperty, "=");
	}
	public static <T> Criterion<T> neProperty(String property, String otherProperty)
	{
		return new PropertyExpression<T>(property, otherProperty, "<>");
	}
	public static <T> Criterion<T> ltProperty(String property, String otherProperty)
	{
		return new PropertyExpression<T>(property, otherProperty, "<");
	}
	public static <T> Criterion<T> leProperty(String property, String otherProperty)
	{
		return new PropertyExpression<T>(property, otherProperty, "<=");
	}
	public static <T> Criterion<T> gtProperty(String property, String otherProperty)
	{
		return new PropertyExpression<T>(property, otherProperty, ">");
	}
	public static <T> Criterion<T> geProperty(String property, String otherProperty)
	{
		return new PropertyExpression<T>(property, otherProperty, ">=");
	}
	public static <T> Criterion<T> isNotNull(String property)        { return new NotNullExpression<T>(property); }
	public static <T> Criterion<T> and(Criterion<T> lhs, Criterion<T> rhs)
	{
		return new LogicalExpression<T>(lhs, rhs, "and");
	}
	public static <T> Criterion<T> or(Criterion<T> lhs, Criterion<T> rhs)
	{
		return new LogicalExpression<T>(lhs, rhs, "or");
	}
	public static <T> Criterion<T> not(Criterion<T> expression)
	{
		return new NotExpression<T>(expression);
	}
	public static <T> Conjunction<T> conjunction()
	{
		return new Conjunction<T>();
	}
	public static <T> Disjunction<T> disjunction()
	{
		return new Disjunction<T>();
	}
	public static <T> Criterion<T> isEmpty   (String property) { return new EmptyExpression<T>(property); }
	public static <T> Criterion<T> isNotEmpty(String property)
	{
		return new NotEmptyExpression<T>(property);
	}
	public static <T> Criterion<T> sizeEq(String property, long size)
	{
		return new SizeExpression<T>(property, size, "=" );
	}
	public static <T> Criterion<T> sizeNe(String property, long size)
	{
		return new SizeExpression<T>(property, size, "<>");
	}
	public static <T> Criterion<T> sizeGt(String property, long size)
	{
		return new SizeExpression<T>(property, size, "<" );
	}
	public static <T> Criterion<T> sizeLt(String property, long size)
	{
		return new SizeExpression<T>(property, size, ">" );
	}
	public static <T> Criterion<T> sizeGe(String property, long size)
	{
		return new SizeExpression<T>(property, size, "<=");
	}
	public static <T> Criterion<T> sizeLe(String property, long size)
	{
		return new SizeExpression<T>(property, size, ">=");
	}
	public static <T> Criterion<T> allEq(Map<?, ?> propertyValues)
	{
		Conjunction<T> conj = conjunction();
		Iterator<?> iter = propertyValues.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry me = (Map.Entry) iter.next();
			conj.add(eq((String) me.getKey(), me.getValue()));
		}
		return conj;
	}
}