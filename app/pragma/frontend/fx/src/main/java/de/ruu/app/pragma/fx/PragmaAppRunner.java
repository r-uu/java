package de.ruu.app.pragma.fx;

import de.ruu.lib.fx.comp.FXCAppRunner;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class PragmaAppRunner extends FXCAppRunner
{
    private static final Logger log = LogManager.getLogger(PragmaAppRunner.class);

    public static void main(String[] args)
    {
        log.debug("starting {}", PragmaAppRunner.class.getName());
        FXCAppRunner.configureModuleAccessForCDI();
        PragmaAppRunner.class.getModule().addReads(PragmaAppRunner.class.getClassLoader().getUnnamedModule());
        FXCAppRunner.run(PragmaApp.class, args);
        log.debug("finished {}", PragmaAppRunner.class.getName());
    }
}