/*
 * Copyright (C) 2008-2012 - Thomas Santana <tms@exnebula.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.exnebula.bootstrap;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.CodeSource;

public class BootConfigLocator {

  public static final String JVM_CONFIG_OPTION = "org.exnebula.bootstrap.config";

  public static File locateFile(Class<?> classToFind, String fileName) {
    File result = locateFileFromSystemProperty();
    if (result != null)
      return result;
    else
      return locateFileAtProtectionDomainParentDirectory(classToFind, fileName);
  }

  private static File locateFileAtProtectionDomainParentDirectory(Class<?> classToFind, String fileName) {
    CodeSource codeSource = classToFind.getProtectionDomain().getCodeSource();
    if (codeSource != null) {
      File sourceFile = normalizedFileFromCodeSource(codeSource);
      File file = new File(sourceFile.getParentFile(), fileName);
      return file.exists() ? file : null;
    } else {
      return null;
    }
  }

  private static File normalizedFileFromCodeSource(CodeSource codeSource) {
    try {
      return new File(URLDecoder.decode(codeSource.getLocation().getFile(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  private static File locateFileFromSystemProperty() {
    if (System.getProperty(JVM_CONFIG_OPTION) != null) {
      File file = new File(System.getProperty(JVM_CONFIG_OPTION));
      if (file.exists())
        return file;
    }
    return null;
  }
}