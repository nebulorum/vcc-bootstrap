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

import org.junit.Test;

import java.io.File;
import static org.junit.Assert.*;

public class BootConfigLocatorTest {

  @Test
  public void getFromClass() {
    File jar = getFileFromProtectionDomain(Test.class);
    String modifiedFileName = replaceExtension(jar.getName(), "pom");

    File result = BootConfigLocator.locateFile(Test.class, modifiedFileName);
    assertEquals(new File(jar.getParentFile(), modifiedFileName), result);
    assertTrue(result.isFile());
  }

  @Test
  public void getFromClassButNotInPath() {
    assertNull(BootConfigLocator.locateFile(Test.class, "not-found.file"));
  }

  @Test
  public void findFileFromSystemProperty() {
    System.setProperty(BootConfigLocator.JVM_CONFIG_OPTION, "./pom.xml");
    File result = BootConfigLocator.locateFile(Test.class, "pom.xml");
    assertEquals(new File(".", "pom.xml"), result);
  }

  @Test
  public void nullIfCannotFindFromSystemProperty() {
    System.setProperty(BootConfigLocator.JVM_CONFIG_OPTION, "./not-found.txt");
    File result = BootConfigLocator.locateFile(Test.class, "./not-found.txt");
    assertNull(result);
  }

  @Test
  public void findPropertyFirst() {
    File jar = getFileFromProtectionDomain(Test.class);
    System.setProperty(BootConfigLocator.JVM_CONFIG_OPTION, "./pom.xml");

    String modifiedFileName = replaceExtension(jar.getName(), "pom");

    File result = BootConfigLocator.locateFile(Test.class, modifiedFileName);
    assertEquals(new File(".", "pom.xml"), result);
  }

  private File getFileFromProtectionDomain(Class<?> targetClass) {
    return new File(targetClass.getProtectionDomain().getCodeSource().getLocation().getFile());
  }

  private String replaceExtension(String original, String newExtension) {
    return original.substring(0, original.lastIndexOf('.') + 1) + newExtension;
  }
}