package de.ruu.lib.archunit;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public interface Util
{
	static boolean isPublic(JavaMethod method)
	{
		return
				method
						.getModifiers()
						.stream()
						.filter(m -> m.equals(JavaModifier.PUBLIC))
						.findFirst()
						.isPresent();
	}

	static List<FieldWithAccessors> fieldsWithAccessors(Class<?> clazz)
	{
		List<FieldWithAccessors> result = new ArrayList<>();

		JavaClass javaClass = new ClassFileImporter().importClass(clazz);
		javaClass.getAllFields().forEach(field -> result.add(new FieldWithAccessors(field)));

		return result;
	}

	static boolean isCollection(JavaClass clazz) { return clazz.isAssignableTo(Collection.class); }
	static boolean isCollection(JavaType  type ) { return isCollection(type.toErasure()); }
	static boolean isGeneric   (JavaClass clazz) { return clazz.getTypeParameters().isEmpty() == false; }
	static boolean isGeneric   (JavaType  type ) { return isGeneric(type.toErasure()); }
	static boolean isPrimitive (JavaClass clazz) { return clazz.isPrimitive(); }
	static boolean isPrimitive (JavaType  type ) { return isPrimitive(type.toErasure()); }
	static boolean isNumeric   (JavaClass clazz)
	{
		String name = clazz.getName();

		if (   name.equals(Double .class.getName())
				|| name.equals(Float  .class.getName())
				|| name.equals(Integer.class.getName())
				|| name.equals(Long   .class.getName()))
		{
			return true;
		}
		return false;
	}
	static boolean isNumeric(JavaType     type ) { return isNumeric(type.toErasure()); }

	static Optional<List<JavaType>> actualTypeArguments(JavaType javaType)
	{
		if (javaType instanceof JavaParameterizedType javaParameterisedType)
		{
			return Optional.of(javaParameterisedType.getActualTypeArguments());
		}
		return Optional.empty();
	}

	static Optional<JavaType> firstActualTypeArgument(JavaType javaType)
	{
		if (javaType instanceof JavaParameterizedType javaParameterisedType)
		{
			return Optional.of(javaParameterisedType.getActualTypeArguments().get(0));
		}
		return Optional.empty();
	}
	
	static boolean isParameterisedType(JavaType javaType)
	{
		return javaType instanceof JavaParameterizedType;
	}

	static List<JavaMethod> publicMethodsWithAnnotationAndSortedByName(
			JavaClass source, Class<? extends Annotation> annotation)
	{
		Comparator<JavaMethod> comparator =
				(m1, m2) ->
				m1.getName().compareTo(m2.getName());

		List<JavaMethod> result = new ArrayList<>(source.getAllMethods());

		result.sort(comparator);

		return result;
	}
}