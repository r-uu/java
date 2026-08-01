package de.ruu.app.pragma.rest;

import org.eclipse.microprofile.auth.LoginConfig;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
@LoginConfig(authMethod = "MP-JWT")
public class PragmaApplication extends Application { }
