package de.ruu.lib.jpa.core;

public enum GraphType
{
	FETCH("jakarta.persistence.fetchgraph"),
	LOAD ("jakarta.persistence.loadgraph" );

	private String value;
	private GraphType(String value) { this.value = value; }
	public String value() { return value; }
}