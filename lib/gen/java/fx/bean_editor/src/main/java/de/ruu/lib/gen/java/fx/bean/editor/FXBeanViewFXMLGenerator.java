package de.ruu.lib.gen.java.fx.bean.editor;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import de.ruu.lib.gen.Generator;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.CompilationUnitResourceFileWriter;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.fx.bean.FXMapper;
import de.ruu.lib.gen.java.fx.bean.FXMapper.DeclaredTypeProcessor;
import de.ruu.lib.gen.java.fx.bean.FXProperty;
import de.ruu.lib.gen.java.naming.ImportManager;
import de.ruu.lib.util.Strings;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.ruu.lib.archunit.Util.publicMethodsWithAnnotationAndSortedByName;
import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.StringBuilders.sb;

public class FXBeanViewFXMLGenerator
{
	private static final Logger log = LoggerFactory.getLogger(FXBeanViewFXMLGenerator.class);
	@NonNull private final String packageName;    // package name for target
	@NonNull private final String simpleFileName; // simple file name for target

	@NonNull private final JavaClass source;      // source
	
	private final CompilationUnitContext context;
	private final DeclaredTypeProcessor  processor;

	public FXBeanViewFXMLGenerator(
			@NonNull String packageName, @NonNull String simpleFileName, @NonNull JavaClass clazz)
	{
		if (packageName   .isEmpty()) throw new IllegalArgumentException(    "package name may not be empty");
		if (simpleFileName.isEmpty()) throw new IllegalArgumentException("simple file name may not be empty");

		this.packageName    = packageName;
		this.simpleFileName = simpleFileName;

		this.source         = clazz;

		context   = CompilationUnitContext.context(packageName, simpleFileName);
		processor = FXMapper.importManagement(context);

		ImportManager importManager = context.importManager();

		importManager.useType(ColumnConstraints.class);
		importManager.useType(GridPane         .class);
		importManager.useType(RowConstraints   .class);
	}
	
	public void run() throws GeneratorException, IOException
	{
		StringBuilder controls = controls();
		StringBuilder content  =
				sb("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
						.append(LS)
						.append(LS)
						.append(imports())
						.append(LS)
						.append(controls);
		Generator generator    = Generator.create(content.toString());

		CompilationUnitResourceFileWriter writer =
				CompilationUnitResourceFileWriter.writer(packageName, simpleFileName, "fxml");

		String result = generator.generate().toString();

		log.debug("\n{}", result);

		writer.write(result);
	}

	private StringBuilder imports()
	{
		StringBuilder result = sb();
		
		ImportManager importManager = context.importManager();
		
		List<String> imports = new ArrayList<>();   // prepare imports
		
		for (String key : importManager.typeImports().keySet())
		{
			imports.add(importManager.typeImports().get(key) + "." + key);
		}
		
		imports.sort((i1, i2) -> i1.compareTo(i2)); // sort    imports

		for (String item : imports)                 // process imports
		{
			result
					.append("<?import ")
					.append(item)
					.append("?>")
					.append(LS)
					;
		}

		return result;
	}

	private StringBuilder controls()
	{
		StringBuilder result = sb(gridPane().toString());
		
		return result;
	}

	private StringBuilder gridPane()
	{
		StringBuilder result = sb(
//STR.
//"""
//<GridPane maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308" minHeight="-Infinity" minWidth="-Infinity" xmlns:fx="http://javafx.com/fxml/1" xmlns="http://javafx.com/javafx/19">
//  <columnConstraints>
//    <ColumnConstraints hgrow="SOMETIMES" minWidth="10.0" prefWidth="100.0" />
//    <ColumnConstraints hgrow="SOMETIMES" minWidth="10.0" prefWidth="100.0" />
//  </columnConstraints>
//  <rowConstraints>
//    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
//    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
//    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
//    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
//    <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
//  </rowConstraints>
//\{ children() }
//</GridPane>
//"""
"<GridPane maxHeight=\"1.7976931348623157E308\" maxWidth=\"1.7976931348623157E308\" minHeight=\"-Infinity\" minWidth=\"-Infinity\" xmlns:fx=\"http://javafx.com/fxml/1\" xmlns=\"http://javafx.com/javafx/19\">                                                                 \n" +
"  <columnConstraints>                                                             \n" +
"    <ColumnConstraints hgrow=\"SOMETIMES\" minWidth=\"10.0\" prefWidth=\"100.0\"/>\n" +
"    <ColumnConstraints hgrow=\"SOMETIMES\" minWidth=\"10.0\" prefWidth=\"100.0\"/>\n" +
"  </columnConstraints>                                                            \n" +
"  <rowConstraints>                                                                \n" +
"    <RowConstraints minHeight=\"10.0\" prefHeight=\"30.0\" vgrow=\"SOMETIMES\" /> \n" +
"    <RowConstraints minHeight=\"10.0\" prefHeight=\"30.0\" vgrow=\"SOMETIMES\" /> \n" +
"    <RowConstraints minHeight=\"10.0\" prefHeight=\"30.0\" vgrow=\"SOMETIMES\" /> \n" +
"    <RowConstraints minHeight=\"10.0\" prefHeight=\"30.0\" vgrow=\"SOMETIMES\" /> \n" +
"    <RowConstraints minHeight=\"10.0\" prefHeight=\"30.0\" vgrow=\"SOMETIMES\" /> \n" +
"  </rowConstraints>" +
children() +
"</GridPane>"
		);
		return result;
	}

	private StringBuilder children()
	{
		StringBuilder result = sb();
		result.append("  <children>");
		result.append(LS);
		result.append(childrenLabel());
		result.append(childrenOther());
		result.append("  </children>");
		return result;
	}

	private StringBuilder childrenLabel()
	{
		StringBuilder result = sb();

		int rowIndex = 0;

		ImportManager importManager = context.importManager();

		for (JavaMethod method : publicMethodsWithAnnotationAndSortedByName(source, FXProperty.class))
		{
			// generate labels only for methods that return a type that matches to a supported fx control
			Optional<String> optional = FXMapper.mapToFXControl(method.getReturnType(), processor);

			if (optional.isPresent())
			{
				String fxId = "lbl" + Strings.firstLetterToUpperCase(method.getName());
				String text = method.getName();

				result.append(
						"    <" + importManager.useType(Label.class) + " "
								+ "fx:id=\"" + fxId + "\" "
								+ "text=\"" + text + "\" "
								+ "GridPane.rowIndex=\"" + rowIndex + "\"/>"
								+ LS);
				rowIndex++;
			}
		}

		return result;
	}

	private StringBuilder childrenOther()
	{
		StringBuilder result = sb();
		
		int rowIndex = 0;
		int colIndex = 1;

		ImportManager importManager = context.importManager();

		for (JavaMethod method : publicMethodsWithAnnotationAndSortedByName(source, FXProperty.class))
		{
			Optional<String> optional = FXMapper.mapToFXControl(method.getReturnType(), processor);
			
			if (optional.isPresent())
			{
				String controlSimpleName = optional.get();
				String fxId = "";

				if      (controlSimpleName.equals(TextField.class.getSimpleName()))
				{
					importManager.useType(TextField.class);
					fxId = "tf"; 
				}
				else if (controlSimpleName.equals(CheckBox.class.getSimpleName()))
				{
					importManager.useType(CheckBox.class);
					fxId = "chkBx"; 
				}
				else if (controlSimpleName.equals(Spinner.class.getSimpleName()))
				{
					importManager.useType(Spinner.class);
					fxId = "spnnr"; 
				}
				else if (controlSimpleName.equals(ComboBox.class.getSimpleName()))
				{
					importManager.useType(ComboBox.class);
					fxId = "cmbBx"; 
				}
				else
				{
					log.error("unexpected fx control type {}, skipping generation", controlSimpleName);
					continue; // TODO remove code smell
				}

				fxId += Strings.firstLetterToUpperCase(method.getName());

				result.append(
						"    <"
						+ controlSimpleName + " fx:id=\"" + fxId + "\" "
						+ importManager.useType(GridPane.class) + ".columnIndex=\"" + colIndex + "\" "
						+ importManager.useType(GridPane.class) + ".rowIndex=\""    + rowIndex + "\"/>"
						+ LS);

				rowIndex++;
			}
			else
			{
				log.warn("no control found for return type {} of method {}", method.getReturnType().getName(), method.getName());
			}
		}
		
		return result;
	}
}