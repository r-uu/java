package de.ruu.lib.cdi.se;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestBean
{
  public String hello() { return "hello"; }
}
