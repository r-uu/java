package de.ruu.lib.gen.java.naming;

import static de.ruu.lib.gen.java.naming.GeneratorImportStatement.importStatement;
import static de.ruu.lib.util.StringBuilders.rTrimChars;
import static de.ruu.lib.util.StringBuilders.sb;
import static de.ruu.lib.util.Types.packageNameFromQualifiedTypeName;
import static de.ruu.lib.util.Types.simpleNameFromQualifiedTypeName;
import static de.ruu.lib.util.lang.model.JavaLangMetaModel.asDeclaredType;
import static de.ruu.lib.util.lang.model.JavaLangMetaModel.asTypeElement;
import static de.ruu.lib.util.lang.model.JavaLangMetaModel.hasTypeArguments;
import static de.ruu.lib.util.lang.model.JavaLangMetaModel.hasTypeParameters;
import static de.ruu.lib.util.lang.model.JavaLangMetaModel.isPrimitiveKind;
import static java.lang.System.lineSeparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import de.ruu.lib.gen.java.JavaType;
import de.ruu.lib.gen.java.JavaTypeDeclared;
import de.ruu.lib.gen.java.JavaTypePrimitive;
import de.ruu.lib.util.StringBuilders;
import de.ruu.lib.util.Strings;
import org.jspecify.annotations.NonNull;

public class ImportManager
{
	/**
	 * Stores a package name of the generation target that this import manager is
	 * created for. {@link #generateImportStatements()} will not generate import
	 * statements for types from the corresponding package.
	 */
	private String targetCompilationUnitPackageName;

	/**
	 * Stores the simple (non qualified) name of the generation target that this
	 * import manager is created for.
	 */
	private String targetCompilationUnitSimpleName;

	/**
	 * Stores qualified names for generation targets by type elements. If a target
	 * is to be generated for one of the type elements in the key set of this map
	 * import manager will use the corresponding value for the generation of
	 * import statements.
	 */
	private Map<TypeElement, String> typeElementToTargetQualifiedTypeName;

	/**
	 * Package names for which {@link #useType(String)} will always return simple
	 * names.
	 */
	private Set<String> forceSimpleNamesForPackages = new HashSet<>();

	/**
	 * Stores pairs of simple type names and their corresponding package name. The
	 * simple type names can be used unqualified in the java type to be generated
	 * because an appropriate type import statement will be returned by {@link
	 * #generateImportStatements()}.
	 */
	private Map<String, String> typeImports = new HashMap<>();

	/** stores fully qualified static names. */
	private Set<String> fullyQualifiedStaticNames = new TreeSet<>();

	public String                    targetCompilationUnitPackageName()      { return targetCompilationUnitPackageName; }
	public String                    targetCompilationUnitSimpleName()        { return targetCompilationUnitSimpleName; }
	public Map<TypeElement, String>  typeElementToTargetQualifiedTypeName()  { return typeElementToTargetQualifiedTypeName; }
	public Set<String>               forceSimpleNamesForPackages()           { return forceSimpleNamesForPackages; }
	public Set<String>               fullyQualifiedStaticNames()             { return fullyQualifiedStaticNames; }

	public ImportManager(
			String targetCompilationUnitPackageName,
			String targetCompilationUnitSimpleName,
			Map<TypeElement, String> typeElementToTargetQualifiedTypeName)
	{
		if (targetCompilationUnitPackageName == null)
		{
			this.targetCompilationUnitPackageName = "";
		}
		else
		{
			this.targetCompilationUnitPackageName = targetCompilationUnitPackageName;
		}

		if (targetCompilationUnitSimpleName == null)
		{
			this.targetCompilationUnitSimpleName = "";
		}
		else
		{
			this.targetCompilationUnitSimpleName = targetCompilationUnitSimpleName;
		}

		if (typeElementToTargetQualifiedTypeName == null)
		{
			this.typeElementToTargetQualifiedTypeName = new HashMap<>();
		}
		else
		{
			this.typeElementToTargetQualifiedTypeName =
					typeElementToTargetQualifiedTypeName;
		}

		forceSimpleNamesForPackages.add("java.lang");
	}

	public ImportManager(@NonNull String packagename, @NonNull String targetCompilationUnitSimpleName)
	{
		this(packagename, targetCompilationUnitSimpleName, null);
	}

	public void addPackageToForceSimpleNamesForPackages(String packagename)
	{
		if (Strings.isNullOrEmpty(packagename)) { return; }
		forceSimpleNamesForPackages.add(packagename);
	}

	public Map<String, String> typeImports()
	{
		return Collections.unmodifiableMap(typeImports);
	}

	/**
	 * @return import statements, containing static and non static imports for the
	 *         java type to be generated
	 */
	public StringBuilder generateImportStatements()
	{
		StringBuilder result = sb();

		for (String importStatement : importStatements())
		{
			result.append(importStatement);
		}

		return StringBuilders.rTrimChars(result, lineSeparator());
	}

	/**
	 * Updates internal data as necessary and calculates string for type
	 * information in generated field or parameter declarations. For types from
	 * package <code>java.lang</code> and for types from the same package as
	 * {@link #targetCompilationUnitPackageName} this method will calculate the according
	 * simple name of <code>qualifiedImportTypeName</code>.
	 *
	 * @param qualifiedImportTypeName
	 * @return <code>qualifiedImportTypeName</code> if it has to be used
	 *         qualified in generated code, simple type name if it does not have
	 *         to be used qualified.
	 */
	public String useType(String qualifiedImportTypeName)
	{
		String packagename =
				packageNameFromQualifiedTypeName(qualifiedImportTypeName);

		if (forceSimpleNamesForPackages.contains(packagename))
		{
			// do not generate import statements for java types from default package
			// java.lang
			return simpleNameFromQualifiedTypeName(qualifiedImportTypeName);
		}

		if (false == qualifiedImportTypeName.contains("."))
		{
			// no package in qualifiedImportTypeName
			return qualifiedImportTypeName;
		}

		String simpleName =
				simpleNameFromQualifiedTypeName(qualifiedImportTypeName).replace(
						'$', '.');  // do not use normalizeClassName from
		                    // ProcessingUtilities here because replace is invoked
		                    // for simple types here

		if (targetCompilationUnitSimpleName.equals(simpleName))
		{
			if (targetCompilationUnitPackageName.equals(packagename))
			{
				// the generation target references itself
				return simpleName;
			}
			// the generation target simple name is equal to qualifiedImportTypeName
			// we have to return the complete type name, otherwise it would collide
			// with simpleName
			return qualifiedImportTypeName;
		}

		if (packagename.equals(targetCompilationUnitPackageName))
		{
			// do not generate import statements for java types from the same package
			// as the java type to be generated
			// (at least if there is no type import that will hide the java type in
			// the package)
			if (typeImports.containsKey(simpleName))
			{
				return qualifiedImportTypeName;
			}

			return simpleName;
//			return simpleNameFromQualifiedTypeName(qualifiedImportTypeName);
		}

		String typeImportPackageName = typeImports.get(simpleName);

		if (typeImportPackageName != null)
		{
			// do not add type import for simple names that are already stored in
			// typeImports
			if (typeImportPackageName.equals(packagename))
			{
				// qualifiedImportTypeName had been added to typeImports before and the
				// type may be used unqualified
				return simpleName;
			}
			else
			{
				return qualifiedImportTypeName;
			}
		}

		typeImports.put(simpleName, packagename);

		return simpleName;
	}

	public String useType(Class<?> clazz)
	{
		return useType(clazz.getName());
	}

	public String useType(TypeMirror type)
	{
		if (isPrimitiveKind(type))
		{
			// no imports to be managed for primitive types
			return type.toString();
		}

		TypeElement typeElement = asTypeElement(type);

		if (typeElement != null)
		{
			// let the type element version of this method do the work
			return useType(typeElement);
		}

		// let the string version of this method do the work
		return useType(type.toString());
	}

	public String useType(TypeElement type)
	{
		if (isPrimitiveKind(type))
		{
			// no imports to be managed for built-in primitive types like "int"
			return type.asType().toString();
		}

		if (hasTypeParameters(type))
		{
			DeclaredType declaredType = asDeclaredType(type);

			// let string version of this method do the work for type and ...
			StringBuilder result =
					sb
					(
							useType
							(
									asTypeElement(declaredType).getQualifiedName().toString()
							)
					).append("<");

			List<String> typeParameters = new ArrayList<>();

			for (TypeMirror typeArgument : declaredType.getTypeArguments())
			{
				if (hasTypeParameters(typeArgument))
				{
					// let the type mirror version of this method do the work for the
					// type argument (this can indirectly lead to a recursive call of this
					// method!)
					typeParameters.add(useType(typeArgument));
				}
				else
				{
					// let the string version of this method do the work for the
					// type argument
					DeclaredType declaredTypeInner = asDeclaredType(typeArgument);
					typeParameters.add(
							useType(
									asTypeElement(declaredTypeInner).getQualifiedName().toString()));
				}
			}

			// join the collected type parameters and return the resulting string
			return
					result.append(String.join(",", typeParameters))
					      .append('>').toString();
		}

		// let the string version of this method do the work for the type argument
		return useType(type.asType().toString());
	}

	public String useTypeOf(Element element)
	{
		DeclaredType declaredType = asDeclaredType(element.asType());

		if (declaredType == null)
		{
			// no imports to be managed for built-in primitive types like "int"
			return element.asType().toString();
		}

		// try to find element in custom mapping
		String generationTargetQualifiedName =
				typeElementToTargetQualifiedTypeName.get(element);

		if (generationTargetQualifiedName != null)
		{
			// delegate to useType(String) method
			return useType(generationTargetQualifiedName);
		}

		if (hasTypeArguments(declaredType))
		{
			// delegate to useType(String) method for
			StringBuilder result =
					sb
					(
							useType
							(
									asTypeElement(declaredType).getQualifiedName().toString()
							)
					).append("<");

			List<String> typeParameters = new ArrayList<>();

			for (TypeMirror typeArgument : declaredType.getTypeArguments())
			{
				if (hasTypeParameters(typeArgument))
				{
					// delegate to useType(TypeMirror)
					// (this might indirectly lead to a recursive call of this method!)
					typeParameters.add(useType(typeArgument));
				}
				else
				{
					// recurse
					DeclaredType declaredTypeInner = asDeclaredType(typeArgument);
					typeParameters.add(useTypeOf(asTypeElement(declaredTypeInner)));
				}
			}

			// join the collected type parameters and return the resulting string
			return
					result.append(String.join(",", typeParameters))
					      .append('>').toString();
		}

		return useType(asTypeElement(declaredType));
	}

	public void addStaticImport(String staticImport)
	{
		if (staticImport.endsWith("*"))
		{
			fullyQualifiedStaticNames.add(staticImport);
		}
		else
		{
			int index = staticImport.lastIndexOf('.');

			if (index > 0)
			{
				String wildcardImport = staticImport.substring(0, index) + ".*";

				if (fullyQualifiedStaticNames.contains(wildcardImport))
				{
					// there already is a wildcard import for staticImport
				}
				else
				{
					fullyQualifiedStaticNames.add(staticImport);
				}
			}
		}
	}

	public List<GeneratorImportStatement> importStatementGenerators()
	{
		List<GeneratorImportStatement> result = new ArrayList<>();

		for (String importStatement : importStatements())
		{
			result.add(importStatement(importStatement));
		}

		return result;
	}

	public String asString(JavaType type)
	{
		if (type == null)
		{
			return "";
		}
		else if (type instanceof JavaTypePrimitive)
		{
			if (type.isArrayType())
			{
				return ((JavaTypePrimitive) type).asString() + "[]";
			}
			else
			{
				return ((JavaTypePrimitive) type).asString();
			}
		}
		else if (type instanceof JavaTypeDeclared)
		{
			String result = asString((JavaTypeDeclared) type);

			if (type.isArrayType())
			{
				return result + "[]";
			}
			else
			{
				return result;
			}
		}
		else
		{
			throw new IllegalStateException("unexpected java type " + type.getClass().getName());
		}
	}

	/** recursive implementation */
	public String asString(JavaTypeDeclared type)
	{
		String result = useType(type.getDeclaredType());

		if (type.getTypeParameters().isPresent())
		{
			StringBuilder typeParametersAsString = StringBuilders.sb();

			for (JavaTypeDeclared typeParameter : type.getTypeParameters().get())
			{
				// enter recursion
				typeParametersAsString.append(asString(typeParameter) + ", ");
			}

			typeParametersAsString = rTrimChars(typeParametersAsString, ", ");

			result += "<" + typeParametersAsString + ">";
		}

		return result;
	}

	private List<String> importStatements()
	{
		List<String> result = new ArrayList<>();

		// add static import statements to result
		for (String staticImport : fullyQualifiedStaticNames)
		{
			result.add("import static " + staticImport + ";" + lineSeparator());
		}

		// collect qualified type names to import
		Set<String> importClasses = new TreeSet<>();

		for (String simpleTypeName : typeImports.keySet())
		{
			importClasses.add(typeImports.get(simpleTypeName) + "." + simpleTypeName);
		}

		// add non static, qualified type names to result
		for (String importClass : importClasses)
		{
			result.add("import " + importClass + ";" + lineSeparator());
		}

		return result;
	}
}