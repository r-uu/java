package de.ruu.app.pragma.fx.task.gantt;

import de.ruu.lib.fx.comp.DefaultFXCView;
import jakarta.enterprise.context.Dependent;

@Dependent
public class Gantt extends DefaultFXCView<Gantt, GanttService, GanttController>
{
    public GanttController getController() { return controller(); }
}
