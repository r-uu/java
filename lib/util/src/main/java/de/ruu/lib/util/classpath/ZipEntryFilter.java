package de.ruu.lib.util.classpath;

import java.util.zip.ZipEntry;

public interface ZipEntryFilter
{
  boolean accept(ZipEntry zipEntry);
}