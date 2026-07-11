package de.ruu.lib.gen.java;

/** provides information about a primitive Java type */
public interface JavaTypePrimitive extends JavaType
{
	TypeKindPrimitive typeKind();
	JavaType setTypeKind(TypeKindPrimitive typeKind);

	default String asString()
	{
		if (isArrayType())
		{
			return typeKind().asString() + "[]";
		}
		else
		{
			return typeKind().asString();
		}
	}
	
	static String toWrapper(String primitive)
	{
		if (primitive.equals("boolean")) return Boolean.class.getName();
		if (primitive.equals("double" )) return Double .class.getName();
		if (primitive.equals("float"  )) return Float  .class.getName();
		if (primitive.equals("int"    )) return Integer.class.getName();
		if (primitive.equals("long"   )) return Long   .class.getName();
		throw new IllegalArgumentException("unexpected primitive type: " + primitive);
	}
}