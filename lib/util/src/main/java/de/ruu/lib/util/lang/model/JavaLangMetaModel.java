package de.ruu.lib.util.lang.model;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleElementVisitor14;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.SimpleTypeVisitor7;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public abstract class JavaLangMetaModel
{
	/** Private constructor to prevent instantiation of utility class. */
	private JavaLangMetaModel() { throw new AssertionError("utility class"); }

	/**
	 * Class for {@link TypeVisitor} objects that provide a {@link DeclaredType} instance if the objects are accepted by
	 * {@link TypeMirror#accept(TypeVisitor, Object)}. Provided instances are <code>null</code> if accepting <code>
	 * TypeMirror</code> objects do not have {@link TypeKind#DECLARED}.
	 */
	private static class DeclaredTypeProviderVisitor extends SimpleTypeVisitor14<DeclaredType, Void>
	{
		@Override public DeclaredType visitDeclared(DeclaredType declaredType, Void parameter)
		{
			return declaredType;
		}
	}

	private static class ArrayTypeProviderVisitor extends SimpleTypeVisitor14<ArrayType, Void>
	{
		/**
		 * @see javax.lang.model.util.SimpleTypeVisitor6#visitArray(ArrayType, Object)
		 */
		@Override public ArrayType visitArray(ArrayType arrayType, Void parameter)
		{
			return arrayType;
		}
	}

	/**
	 * Class for {@link ElementVisitor} objects that provide a {@link TypeElement} instance if the objects are accepted by
	 * {@link Element#accept(ElementVisitor, Object)}. Provided instances are <code>null</code> if accepting <code>Element
	 * </code> objects do not have {@link ElementKind#CLASS}.
	 *
	 * TODO find out why string list and integer objects are not visited as types
	 *      in SimpleElementVisitor6
	 */
	private static class TypeElementProviderVisitor extends SimpleElementVisitor14<TypeElement, Void>
	{
		@Override public TypeElement visitType(TypeElement typeElement, Void parameter)
		{
			return typeElement;
		}
	}

	private static class ExecutableElementProviderVisitor extends SimpleElementVisitor14<ExecutableElement, Void>
	{
		@Override public ExecutableElement visitExecutable(ExecutableElement executableElement, Void parameter)
		{
			return executableElement;
		}
	}

	private static class PackageElementProviderVisitor extends SimpleElementVisitor14<PackageElement, Void>
	{
		@Override public PackageElement visitPackage(PackageElement packageElement, Void parameter)
		{
			return packageElement;
		}
	}

	private static class VariableElementProviderVisitor extends SimpleElementVisitor14<VariableElement, Void>
	{
		@Override public VariableElement visitVariable(VariableElement variableElement, Void parameter)
		{
			return variableElement;
		}
	}

	/**
	 * <code>TypeVisitor</code> object that provides {@link DeclaredType} instance when it is accepted by {@link
	 * TypeMirror#accept(TypeVisitor, Object)} and <code>TypeMirror</code> object does not have {@link TypeKind#DECLARED}.
	 */
	private final static DeclaredTypeProviderVisitor DECLARED_TYPE_PROVIDER_VISITOR = new DeclaredTypeProviderVisitor();
	private final static ArrayTypeProviderVisitor ARRAY_TYPE_PROVIDER_VISITOR = new ArrayTypeProviderVisitor();
	private final static PackageElementProviderVisitor PACKAGE_ELEMENT_PROVIDER_VISITOR = new PackageElementProviderVisitor();
	/**
	 * <code>TypeVisitor</code> object that provides {@link DeclaredType} instance when it is accepted by {@link
	 * TypeMirror#accept(TypeVisitor, Object)} and <code>TypeMirror</code> object does not have {@link TypeKind#DECLARED}.
	 */
	private final static TypeElementProviderVisitor TYPE_ELEMENT_PROVIDER_VISITOR = new TypeElementProviderVisitor();
	private final static ExecutableElementProviderVisitor EXECUTABLE_ELEMENT_PROVIDER_VISITOR = new ExecutableElementProviderVisitor();
	private final static VariableElementProviderVisitor VARIABLE_ELEMENT_PROVIDER_VISITOR = new VariableElementProviderVisitor();

	public static boolean isPrimitiveKind(TypeMirror typeMirror) { return typeMirror.getKind().isPrimitive(); }
	public static boolean isPrimitiveKind(Element element) { return element.asType().getKind().isPrimitive(); }
	public static boolean hasTypeArguments(DeclaredType type) { return type.getTypeArguments().size() > 0; }
	public static boolean hasTypeParameters(TypeElement type) { return type.getTypeParameters().size() > 0; }

	public static boolean hasTypeParameters(TypeMirror type)
	{
		TypeElement typeElement = asTypeElement(type);

		if (typeElement != null) return hasTypeParameters(typeElement);
		else return false;
	}

	/**
	 * @param typeMirror
	 * @return <code>DeclaredType</code> instance if <code>typeMirror</code> object has {@link TypeKind#DECLARED}, <code>
	 *         null</code> otherwise
	 */
	public static DeclaredType asDeclaredType(TypeMirror typeMirror)
	{
		return typeMirror.accept(DECLARED_TYPE_PROVIDER_VISITOR, null);
	}

	/**
	 * @see #asDeclaredType(TypeMirror)
	 */
	public static DeclaredType asDeclaredType(TypeElement type)
	{
		return asDeclaredType(type.asType());
	}

	public static ArrayType asArrayType(TypeMirror typeMirror)
	{
		return typeMirror.accept(ARRAY_TYPE_PROVIDER_VISITOR, null);
	}

	public static PackageElement asPackageElement(Element element)
	{
		return element.accept(PACKAGE_ELEMENT_PROVIDER_VISITOR, null);
	}

	/**
	 * @param element
	 * @return <code>TypeElement</code> instance if <code>element</code> object has {@link ElementKind#CLASS},
	 *         <code>null</code> otherwise
	 */
	public static TypeElement asTypeElement(Element element)
	{
		return element.accept(TYPE_ELEMENT_PROVIDER_VISITOR, null);
	}

	/**
	 * @param element
	 * @return <code>ExecutableElement</code> instance if <code>element</code> object has {@link ElementKind#METHOD},
	 *         <code>null</code> otherwise
	 */
	public static ExecutableElement asExecutableElement(Element element)
	{
		return element.accept(EXECUTABLE_ELEMENT_PROVIDER_VISITOR, null);
	}

	/**
	 * @param element
	 * @return <code>VariableElement</code> instance if <code>element</code> object has {@link ElementKind#FIELD},
	 * <code>null</code> otherwise
	 */
	public static VariableElement asVariableElement(Element element)
	{
		return element.accept(VARIABLE_ELEMENT_PROVIDER_VISITOR, null);
	}

	/**
	 * @param typeMirror
	 * @return <code>TypeElement</code> instance if <code>typeMirror</code> represents a <code>TypeElement</code>,
	 *         <code>null</code> otherwise
	 */
	public static TypeElement asTypeElement(TypeMirror typeMirror)
	{
		DeclaredType declaredType = asDeclaredType(typeMirror);

		if (declaredType == null)
		{
			return null;
		}

		return asTypeElement(declaredType.asElement());
	}

	/**
	 * recursive implementation!
	 *
	 * @param element
	 * @return
	 */
	public static PackageElement packageElementOf(Element element)
	{
		PackageElement result = asPackageElement(element);

		if (result != null)
		{
			return result;
		}
		else
		{
			// recurse
			return packageElementOf(element.getEnclosingElement());
		}
	}

	public static List<VariableElement> fields(TypeElement typeElement)
	{
		return fieldsIn(typeElement.getEnclosedElements());
	}

	public static List<ExecutableElement> methods(TypeElement typeElement)
	{
		return methodsIn(typeElement.getEnclosedElements());
	}

	public static Set<TypeElement> withEnclosedTypeElements(Set<TypeElement> typeElements)
	{
		Set<TypeElement> result = new HashSet<>();

		for (TypeElement typeElement : typeElements)
		{
			// add type element
			result.add(typeElement);
			// add enclosed type elements recursively
			result.addAll(withEnclosedTypeElements(typeElements(typeElement.getEnclosedElements())));
		}

		return result;
	}

	public static String normalizeBinaryClassName(Class<?> clazz)
	{
		return normalizeBinaryQualifiedTypeName(clazz.getName());
	}

	public static String normalizeBinaryQualifiedTypeName(String name)
	{
//		return name.replaceAll("\\$", "\\.");
		return name.replace('$', '.');
	}

	public static Set<Element> asElementSet(Set<TypeElement> typeElements)
	{
		Set<Element> result = new HashSet<>();
		result.addAll(typeElements);
		return result;
	}

	public static Set<Element> asElementSet(List<Element> elements)
	{
		Set<Element> result = new HashSet<>();
		result.addAll(elements);
		return result;
	}

	public static boolean isInnerType(TypeElement typeElement)
	{
		ElementKind kind = typeElement.getEnclosingElement().getKind();
		return kind.isClass() || kind.isInterface();
	}

	/**
	 * @param elements
	 * @return those elements in <code>elements</code> that are <code>
	 *         TypeElements</code>
	 */
	public static Set<TypeElement> typeElements(Collection<? extends Element> elements)
	{
		Set<TypeElement> result = new HashSet<TypeElement>();

		for (Element element : elements)
		{
			if (element instanceof TypeElement)
			{
				result.add((TypeElement) element);
			}
		}

		return result;
	}
}