package de.ruu.app.pragma.fx.admin;

import de.ruu.lib.fx.comp.DefaultFXCView;

public class Admin extends DefaultFXCView<Admin, AdminService, AdminController>
{
    public AdminController getController() { return controller(); }
}
