package de.ruu.app.pragma.fx.task.hierarchy;

import de.ruu.lib.fx.comp.DefaultFXCView;
import jakarta.enterprise.context.Dependent;

@Dependent
public class Hierarchies extends DefaultFXCView<Hierarchies, HierarchiesService, HierarchiesController>
{
    public HierarchiesController getController() { return controller(); }
}
