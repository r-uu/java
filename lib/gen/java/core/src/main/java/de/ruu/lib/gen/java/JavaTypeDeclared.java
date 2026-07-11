package de.ruu.lib.gen.java;

import java.util.List;
import java.util.Optional;

/** provides information about a declared Java type */
public interface JavaTypeDeclared extends JavaType
{
	JavaTypeDeclared setDeclaredType(Class<?> declaredType);
	Class<?> getDeclaredType();
	// TODO add support for "? extends Type"

	Optional<List<JavaTypeDeclared>> getTypeParameters();
	JavaTypeDeclared setTypeParameters(List<JavaTypeDeclared> typeParameters);
	void addTypeParameter(JavaTypeDeclared typeParameter);
}