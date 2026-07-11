package de.ruu.lib.gen.java;

import javax.lang.model.element.ElementKind;

public enum TypeKindDeclared
{
	ENUM(      ElementKind.ENUM),
	CLASS(     ElementKind.CLASS),
	ANNOTATION(ElementKind.ANNOTATION_TYPE),
	INTERFACE((ElementKind.INTERFACE))
	;

	private ElementKind elementKind;
	private TypeKindDeclared(ElementKind elementKind) { this.elementKind = elementKind; }

	public String asString()
	{
		if (elementKind == ElementKind.ANNOTATION_TYPE) return "@" + ElementKind.INTERFACE.toString().toLowerCase();
		return elementKind.toString().toLowerCase();
//		if (elementKind == ElementKind.ENUM)            return       ElementKind.ENUM     .toString().toLowerCase();
//		if (elementKind == ElementKind.CLASS)           return       ElementKind.CLASS    .toString().toLowerCase();
//		if (elementKind == ElementKind.ANNOTATION_TYPE) return "@" + ElementKind.INTERFACE.toString().toLowerCase();
//		return                                                       ElementKind.INTERFACE.toString().toLowerCase();
	}

	public ElementKind elementKind() { return elementKind; }
}