package de.ruu.lib.fx.css;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static de.ruu.lib.fx.css.TableViewSelector.COLUMN_HEADER;
import static de.ruu.lib.fx.css.TableViewSelector.LABEL;

public class TableViewUtil
{
	@NonNull private final TableView<?> tableView;

	public TableViewUtil(@NonNull TableView<?> tableView)
	{
		this.tableView = Objects.requireNonNull(tableView, "tableView");
	}

	public Set<TableColumnHeader> tableColumnHeaders()
	{
		Set<TableColumnHeader> result = new HashSet<>();

		for (Node node : tableView.lookupAll(COLUMN_HEADER.value))
		{
			if (node instanceof TableColumnHeader)
			{
				result.add((TableColumnHeader) node);
			}
		}

		return result;
	}

	public Set<Label> tableColumnHeaderLabels()
	{
		Set<Label> result = new HashSet<>();

		for (TableColumnHeader tableColumnHeader : tableColumnHeaders())
		{
			for (Node node : tableColumnHeader.lookupAll(LABEL.value))
			{
				if (node instanceof Label)
				{
					result.add(((Label) node));
				}
			}
		}

		return result;
	}
}
