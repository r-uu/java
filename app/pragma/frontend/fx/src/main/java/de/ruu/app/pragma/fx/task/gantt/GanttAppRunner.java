package de.ruu.app.pragma.fx.task.gantt;

import de.ruu.lib.fx.comp.FXCAppRunner;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class GanttAppRunner extends FXCAppRunner
{
    private static final Logger log = LogManager.getLogger(GanttAppRunner.class);

    public static void main(String[] args)
    {
        log.debug("starting {}", GanttAppRunner.class.getName());
        FXCAppRunner.configureModuleAccessForCDI();
        GanttAppRunner.class.getModule().addReads(GanttAppRunner.class.getClassLoader().getUnnamedModule());
        FXCAppRunner.run(GanttApp.class, args);
        log.debug("finished {}", GanttAppRunner.class.getName());
    }
}
