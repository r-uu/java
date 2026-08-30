package de.ruu.app.pragma.fx.admin;

import de.ruu.lib.fx.comp.DefaultFXCView;
import jakarta.enterprise.context.Dependent;

@Dependent
public class Admin extends DefaultFXCView<Admin, AdminService, AdminController>
{
    public AdminController getController() { return controller(); }
}
