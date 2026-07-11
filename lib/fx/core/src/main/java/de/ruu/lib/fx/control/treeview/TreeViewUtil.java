package de.ruu.lib.fx.control.treeview;

import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.isNull;

public abstract class TreeViewUtil
{
	public static boolean isRoot(@NonNull TreeItem<?> item) { return isNull(item.getParent()); }
}
