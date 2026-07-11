package de.ruu.lib.fx.css;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;

import java.util.HashSet;
import java.util.Set;

public enum TableViewSelector
{
	COLUMN_HEADER,
	LABEL
	;

	private TableViewSelector() { this.value = "." + name().replaceAll("_", "-").toLowerCase(); }

	public final String value;

	public static void main(String[] args) { System.out.println(COLUMN_HEADER.value); }
}