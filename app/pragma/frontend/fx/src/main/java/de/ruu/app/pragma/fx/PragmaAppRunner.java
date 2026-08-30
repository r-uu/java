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
        // Weld SE runs in the unnamed module; allow this module to load Weld proxy classes from it.
        PragmaAppRunner.class.getModule().addReads(PragmaAppRunner.class.getClassLoader().getUnnamedModule());
        try
        {
            PragmaStartupCheck.verify();
            FXCAppRunner.run(PragmaApp.class, args);
        }
        catch (IllegalStateException e)
        {
            System.exit(1);
        }
        log.debug("finished {}", PragmaAppRunner.class.getName());
    }
}