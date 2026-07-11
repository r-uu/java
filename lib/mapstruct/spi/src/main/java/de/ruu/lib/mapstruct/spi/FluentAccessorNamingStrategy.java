package de.ruu.lib.mapstruct.spi;

import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;
import org.mapstruct.ap.spi.MapStructProcessingEnvironment;
import org.mapstruct.ap.spi.util.IntrospectorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Universal AccessorNamingStrategy for MapStruct that handles JavaFX Properties, Fluent Accessors, and Standard JavaBeans.
 * <p>
 * This strategy extends the default MapStruct naming strategy to recognize:
 * <ul>
 *   <li><b>Fluent getters:</b> {@code name()} returns {@code String} (from {@code @Accessors(fluent = true)})</li>
 *   <li><b>Fluent setters:</b> {@code name(String value)} returns {@code this} or {@code void}</li>
 *   <li><b>JavaFX Property accessors:</b> {@code nameProperty()} returns {@code StringProperty}</li>
 *   <li><b>Standard JavaBean accessors:</b> {@code getName()}, {@code setName(String value)}</li>
 * </ul>
 * <p>
 * <b>JavaFX Property Mapping:</b><br>
 * For JavaFX properties, MapStruct will automatically use:
 * <ul>
 *   <li>Reading: {@code nameProperty().get()} or {@code nameProperty().getValue()}</li>
 *   <li>Writing: {@code nameProperty().set(value)} or {@code nameProperty().setValue(value)}</li>
 * </ul>
 * <p>
 * <b>Fluent Accessor Mapping:</b><br>
 * For fluent accessors (Lombok {@code @Accessors(fluent = true)}):
 * <ul>
 *   <li>Reading: {@code name()}</li>
 *   <li>Writing: {@code name(value)}</li>
 * </ul>
 * <p>
 * This allows seamless bidirectional mapping between:
 * <ul>
 *   <li>POJOs with fluent accessors ↔ JavaFX Beans with properties</li>
 *   <li>POJOs with fluent accessors ↔ Standard JavaBeans</li>
 *   <li>JavaFX Beans ↔ Standard JavaBeans</li>
 * </ul>
 *
 * @author r-uu
 */
public class FluentAccessorNamingStrategy extends DefaultAccessorNamingStrategy
{
	@Override
	public void init(MapStructProcessingEnvironment processingEnvironment)
	{
		super.init(processingEnvironment);
	}

	@Override
	public boolean isGetterMethod(ExecutableElement method)
	{
		String methodName = method.getSimpleName().toString();
		boolean hasNoParams = method.getParameters().isEmpty();
		boolean hasReturnType = method.getReturnType().getKind() != TypeKind.VOID;

		if (!hasNoParams || !hasReturnType)
		{
			return false;
		}

		// 1. Check for JavaFX property getter (e.g., nameProperty() returning StringProperty)
		if (methodName.endsWith("Property") && isJavaFXPropertyType(method.getReturnType()))
		{
			return true;
		}

		// 2. Check for fluent getter (e.g., name() returning String)
		//    This will match methods like: String name(), LocalDate start(), etc.
		//    But NOT standard getters like getName() (handled by super)
		if (!methodName.startsWith("get") && 
		    !methodName.startsWith("is") && 
		    !methodName.endsWith("Property"))
		{
			// This is likely a fluent getter
			// Accept it as a getter (MapStruct will figure out if it's valid)
			return true;
		}

		// 3. Fallback to standard getter check (getName(), isActive(), etc.)
		return super.isGetterMethod(method);
	}

	@Override
	public boolean isSetterMethod(ExecutableElement method)
	{
		String methodName = method.getSimpleName().toString();
		boolean hasSingleParam = method.getParameters().size() == 1;

		if (!hasSingleParam)
		{
			return false;
		}

		// 1. JavaFX property methods are NOT setters
		//    (MapStruct will use property().set(value) instead)
		if (methodName.endsWith("Property") && isJavaFXPropertyType(method.getReturnType()))
		{
			return false;
		}

		// 2. Check for fluent setter (e.g., name(String value) returning 'this' or 'void')
		//    Fluent setters don't start with "set"
		if (!methodName.startsWith("set"))
		{
			// This could be a fluent setter
			// Accept it (MapStruct will validate)
			return true;
		}

		// 3. Fallback to standard setter check (setName(String value))
		return super.isSetterMethod(method);
	}

	@Override
	public String getPropertyName(ExecutableElement getterOrSetterMethod)
	{
		String methodName = getterOrSetterMethod.getSimpleName().toString();

		// Handle JavaFX property methods (e.g., nameProperty() -> name)
		if (methodName.endsWith("Property") && 
		    getterOrSetterMethod.getParameters().isEmpty() &&
		    isJavaFXPropertyType(getterOrSetterMethod.getReturnType()))
		{
			String propertyName = methodName.substring(0, methodName.length() - "Property".length());
			
			// Handle readOnlyXxxProperty() -> xxx
			if (propertyName.startsWith("readOnly"))
			{
				propertyName = propertyName.substring("readOnly".length());
			}
			
			return IntrospectorUtils.decapitalize(propertyName);
		}

		// Handle fluent accessors (e.g., name() -> name, start() -> start)
		// If it's not a standard getter/setter, assume it's already the property name
		if (!methodName.startsWith("get") && 
		    !methodName.startsWith("set") && 
		    !methodName.startsWith("is"))
		{
			// Fluent accessor: method name IS the property name
			return methodName;
		}

		// Fallback to default property name extraction (handles getName() -> name, etc.)
		return super.getPropertyName(getterOrSetterMethod);
	}

	/**
	 * Checks if the given type is a JavaFX Property type.
	 */
	private boolean isJavaFXPropertyType(TypeMirror type)
	{
		if (type.getKind() != TypeKind.DECLARED)
		{
			return false;
		}

		Element element = ((DeclaredType) type).asElement();
		String typeName = element.toString();

		// Check if it's a JavaFX property type
		return typeName.startsWith("javafx.beans.property.") &&
		       (typeName.contains("Property") || typeName.contains("ReadOnlyProperty"));
	}
}
