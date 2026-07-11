package de.ruu.lib.gen.java;

import javax.lang.model.type.TypeKind;

public enum TypeKindPrimitive
{
	BOOLEAN(TypeKind.BOOLEAN),
	BYTE   (TypeKind.BYTE   ),
	CHAR   (TypeKind.CHAR   ),
	DOUBLE (TypeKind.DOUBLE ),
	FLOAT  (TypeKind.FLOAT  ),
	INT    (TypeKind.INT    ),
	LONG   (TypeKind.LONG   ),
	SHORT  (TypeKind.SHORT  ),
	VOID   (TypeKind.VOID   ),
	;

	private TypeKind typeKind;
	private TypeKindPrimitive(TypeKind typeKind) { this.typeKind = typeKind; }

	public String asString() { return typeKind.toString().toLowerCase(); }

	public TypeKind typeKind() { return typeKind; }
}