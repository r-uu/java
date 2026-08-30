package de.ruu.app.pragma.fx;

import de.ruu.lib.fx.comp.DefaultFXCView;
import jakarta.enterprise.context.Dependent;

@Dependent
public class Pragma extends DefaultFXCView<Pragma, PragmaService, PragmaController>
{
    public PragmaController getController() { return controller(); }
}
