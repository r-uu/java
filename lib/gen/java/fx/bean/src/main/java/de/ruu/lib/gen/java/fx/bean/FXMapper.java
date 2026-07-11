package de.ruu.lib.gen.java.fx.bean;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.JavaTypeVariable;
import de.ruu.lib.archunit.Util;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.ruu.lib.archunit.Util.firstActualTypeArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface FXMapper
{
	interface DeclaredTypeProcessor
	{
		@NonNull String process (@NonNull String type);
		@NonNull String process (@NonNull Class<?> type);

		static DeclaredTypeProcessor importManagement(CompilationUnitContext context) { return new ImportManagement(context); }
	}

	class ImportManagement implements DeclaredTypeProcessor
	{
		@NonNull private CompilationUnitContext context;

		public ImportManagement(@NonNull CompilationUnitContext context) { this.context = context; }

		@Override public @NonNull String process(@NonNull String type) { return context.importManager().useType(type); }

		@Override public @NonNull String process(@NonNull Class<?> type) { return process(type.getName()); }
	}

	static DeclaredTypeProcessor importManagement(CompilationUnitContext context) { return DeclaredTypeProcessor.importManagement(context); }

	/**
	 * Generates fx property for {@code type} that represents
	 *
	 * <ul>
	 *   <li>primitive data types such as {@code int} ({@link SimpleIntegerProperty} will be generated) or</li>
	 *   <li>declared data types with corresponding fx property types such as {@link Integer} ({@link
	 *       SimpleIntegerProperty} will be generated)</li>
	 *   <li>(custom) declared data types as {@link LocalDate} ({@link
	 *       SimpleObjectProperty} will be generated)</li>
	 *   <li>arrays of above mentioned</li>
	 * </ul>
	 *
	 * @param type
	 * @param processor
	 * @return
	 */
	static Optional<String> mapToFXProperty(@NonNull JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		Optional<String> result = Optional.empty();

		JavaClass typeAsClass = type.toErasure();

		if (typeAsClass.isPrimitive())
		{
			result = mapPrimitiveToFXProperty(type, processor);
		}
		else
		{
			result = Optional.of(mapDeclaredToFXProperty(type, processor));
		}
		
		if (result.isPresent())
		{
			if (typeAsClass.isArray())
			{
				result = Optional.of(result.get() + "[]");
			}
		}

		return result;
	}

	static String mapToFXPropertyImplementation(@NonNull JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		String result;

		JavaClass typeAsClass = type.toErasure();

		if (typeAsClass.isPrimitive())
		{
			result = mapPrimitiveToFXPropertyImplementation(type, processor);
		}
		else
		{
			result = mapDeclaredToFXPropertyImplementation (type, processor);
		}
		
		if (typeAsClass.isArray()) result += "[]";
		
		return result;
	}
	
	static Optional<String> mapToFXControl(@NonNull JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		String fullName = type.toErasure().getFullName();
	
		if      (fullName.equals(String.class.getName()))
		{
			return Optional.of(processor.process(TextField.class));
		}
		else if (fullName.equals(Boolean.class.getName())
		     ||  fullName.equals(Boolean.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(CheckBox.class));
		}
		else if (fullName.equals(BigDecimal.class.getName()))
		{
			return Optional.of(processor.process(TextField.class));
		}
		else if (fullName.equals(Double.class.getName())
		      || fullName.equals(Double.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(TextField.class));
		}
		else if (fullName.equals(Float.class.getName())
	        || fullName.equals(Float.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(TextField.class));
		}
		else if (fullName.equals(Integer.class.getName())
	        || fullName.equals("int"))
		{
			return Optional.of(processor.process(Spinner.class));
		}
		else if (fullName.equals(Long.class.getName())
		      || fullName.equals(Long.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(Spinner.class));
		}
		else if (fullName.equals(List.class.getName()))
		{
			return Optional.of(processor.process(ComboBox.class));
		}
		else
		{
			return Optional.empty();
		}
	}

	static String mapToFXTableCell(@NonNull JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		String fullName = type.toErasure().getFullName();

		if      (fullName.equals(BooleanProperty.class.getName()))
		{
			return processor.process(CheckBoxTableCell.class);
		}
		else
		{
			return processor.process(TextFieldTableCell.class);
		}
	}

	static Optional<String> mapPrimitiveToFXProperty(
			JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		String fullName = type.toErasure().getFullName();

		if      (fullName.equals(Boolean.class.getName())
		     ||  fullName.equals(Boolean.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(BooleanProperty.class));
		}
		else if (fullName.equals(Double.class.getName())
		      || fullName.equals(Double.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(DoubleProperty.class));
		}
		else if (fullName.equals(Float.class.getName())
	        || fullName.equals(Float.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(FloatProperty.class));
		}
		else if (fullName.equals(Integer.class.getName())
	        || fullName.equals("int"))
		{
			return Optional.of(processor.process(IntegerProperty.class));
		}
		else if (fullName.equals(Long.class.getName())
		      || fullName.equals(Long.class.getSimpleName().toLowerCase()))
		{
			return Optional.of(processor.process(LongProperty.class));
		}
		else
		{
			return Optional.empty();
		}
	}

	static Optional<String> mapFXPropertyToPrimitiveWrapper(
			JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		String fullName = type.toErasure().getFullName();

		if      (fullName.equals(BooleanProperty.class.getName()))
		{
			return Optional.of(processor.process(Boolean.class));
		}
		else if (fullName.equals(DoubleProperty.class.getName()))
		{
			return Optional.of(processor.process(Double.class));
		}
		else if (fullName.equals(FloatProperty.class.getName()))
		{
			return Optional.of(processor.process(Float.class));
		}
		else if (fullName.equals(IntegerProperty.class.getName()))
		{
			return Optional.of(processor.process(Integer.class));
		}
		else if (fullName.equals(LongProperty.class.getName()))
		{
			return Optional.of(processor.process(Long.class));
		}
		else
		{
			return Optional.empty();
		}
	}
	
	private static String mapDeclaredToFXProperty(JavaType type, @NonNull DeclaredTypeProcessor processor)
	{
		if (Util.isGeneric(type))
		{
			return mapGeneric(type, processor);
		}
		else
		{
			String fullName = type.toErasure().getFullName();

			if   (fullName.equals(String.class.getName()))
			{
				return processor.process(StringProperty.class);
			}
			else
			{
				return
						processor.process(ObjectProperty.class)
						+ "<"
						+ processor.process(fullName)
						+ ">";
			}
		}
	}

	private static String mapPrimitiveToFXPropertyImplementation(JavaType type, DeclaredTypeProcessor processor)
	{
		String fullName = type.toErasure().getFullName();

		if      (fullName.equals(Boolean.class.getName())
		     ||  fullName.equals(Boolean.class.getSimpleName().toLowerCase()))
		{
			return processor.process(SimpleBooleanProperty.class);
		}
		else if (fullName.equals(Double.class.getName())
		      || fullName.equals(Double.class.getName().toLowerCase()))
		{
			return processor.process(SimpleDoubleProperty.class);
		}
		else if (fullName.equals(Float.class.getName())
	        || fullName.equals(Float.class.getName().toLowerCase()))
		{
			return processor.process(FloatProperty.class);
		}
		else if (fullName.equals(Integer.class.getName())
	        || fullName.equals("int"))
		{
			return processor.process(SimpleIntegerProperty.class);
		}
		else if (fullName.equals(Long.class.getName())
		      || fullName.equals(Long.class.getName().toLowerCase()))
		{
			return processor.process(SimpleLongProperty.class);
		}
		else
		{
			throw new IllegalArgumentException("unexpected primitive java type: " + fullName);
		}
	}

	private static String mapDeclaredToFXPropertyImplementation(JavaType type, DeclaredTypeProcessor processor)
	{
		if (Util.isGeneric(type))
		{
			return mapGenericImplementation(type, processor);
		}
		else
		{
			String fullName = type.toErasure().getFullName();

			if   (fullName.equals(String.class.getName()))
			{
				return processor.process(SimpleStringProperty.class);
			}
			else
			{
				return
						processor.process(SimpleObjectProperty.class)
						+ "<"
						+ processor.process(fullName)
						+ ">";
			}
		}
	}

	private static String mapGeneric(JavaType type, DeclaredTypeProcessor processor)
	{
		JavaClass typeAsClass = type.toErasure();

		if (Util.isCollection(type))
		{
			// find out collection type
			Class<?> collectionType;

			if (typeAsClass.isAssignableTo(Set.class))
					collectionType = SetProperty.class;
			else
					collectionType = ListProperty.class;

			// retrieve generic parameter for collection type (first actual type argument)
			Optional<JavaType> optionalFirstTypeArgument = firstActualTypeArgument(type);
			
			if (optionalFirstTypeArgument.isPresent())
					return
							processor.process(collectionType)
							+ "<"
							+ processor.process(optionalFirstTypeArgument.get().getName())
							+ ">";
			else
					throw new IllegalStateException("unexpected absence of type argument in type: " + type);
		}
		else if (typeAsClass.isAssignableTo(Map.class))
		{
			// find out type parameters for map type
			JavaTypeVariable<JavaClass> typeVariableKey   = typeAsClass.getTypeParameters().get(0);
			JavaTypeVariable<JavaClass> typeVariableValue = typeAsClass.getTypeParameters().get(1);

			String classKey   = typeVariableKey  .toErasure().getFullName();
			String classValue = typeVariableValue.toErasure().getFullName();

			return
					  processor.process(MapProperty.class)
					+ "<"
					+ processor.process(classKey)
					+ ", "
					+ processor.process(classValue)
					+ ">";
		}
		else if (typeAsClass.isAssignableTo(Optional.class))
		{
			JavaType firstActualTypeArgumentOfOptional         = firstActualTypeArgument(type).get();
			String   firstActualTypeArgumentOfOptionalFullName =
					processor.process(firstActualTypeArgumentOfOptional.toErasure().getFullName());

			if (Util.isGeneric(firstActualTypeArgumentOfOptional))
			{
				Optional<JavaType> firstActualTypeArgumentOfGenericTypeForOptional =
						firstActualTypeArgument(firstActualTypeArgumentOfOptional);
				return
						  processor.process(ObjectProperty.class)
						+ "<"
				    +    processor.process(typeAsClass.toErasure().getFullName())
						+    "<"
						+       processor.process(firstActualTypeArgumentOfGenericTypeForOptional.get().toErasure().getFullName())
						+    ">"
						+ ">"
						;
			}

			return
					  processor.process(ObjectProperty.class)
			    + "<"
					+    processor.process(typeAsClass.toErasure().getFullName())
					+ ">"
					;
		}
		else
		{
			throw new IllegalStateException("unexpected generic type: " + type);
		}
	}

	private static String mapGenericImplementation(JavaType type, DeclaredTypeProcessor processor)
	{
		JavaClass typeAsClass = type.toErasure();

		if (Util.isCollection(type))
		{
			// find out collection type
			Class<?> collectionType;

			if (typeAsClass.isAssignableTo(Set.class))
					collectionType = SimpleSetProperty.class;
			else
					collectionType = SimpleListProperty.class;

			// retrieve generic parameter for collection type (first actual type argument)
			Optional<JavaType> optionalFirstTypeArgument = firstActualTypeArgument(type);
			
			if (optionalFirstTypeArgument.isPresent())
					return
							processor.process(collectionType)
							+ "<"
							+ processor.process(optionalFirstTypeArgument.get().getName())
							+ ">";
			else
					throw new IllegalStateException("unexpected absence of type argument in type: " + type);
		}
		else if (typeAsClass.isAssignableTo(Map.class))
		{
			// find out type parameters for map type
			JavaTypeVariable<JavaClass> typeVariableKey   = typeAsClass.getTypeParameters().get(0);
			JavaTypeVariable<JavaClass> typeVariableValue = typeAsClass.getTypeParameters().get(1);

			String classKey   = typeVariableKey  .toErasure().getFullName();
			String classValue = typeVariableValue.toErasure().getFullName();

			return
					  processor.process(SimpleMapProperty.class)
					+ "<"
					+ processor.process(classKey)
					+ ", "
					+ processor.process(classValue)
					+ ">";
		}
		else
		{
			throw new IllegalStateException("unexpected generic type: " + type);
		}
	}
}