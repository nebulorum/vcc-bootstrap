/*
 * Copyright (C) 2008-2013 - Thomas Santana <tms@exnebula.org>
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

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.JVM)
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
  public void findFromJarWithSpaceInProtectedDomain() throws IOException, ClassNotFoundException {
    File destFile = copyClassJarFileToDestination(Test.class, "target/with space/");
    Class<?> aClass = createClassLoaderAndLoadClass(destFile, "org.junit.rules.RuleChain");
    FileUtils.copyFile(new File("pom.xml"), new File("target/with space/boot.cfg"));

    File file = BootConfigLocator.locateFile(aClass, "boot.cfg");
    assertEquals(new File(getJarFileFromClass(aClass).getParent(), "boot.cfg"), file);
  }

  @Test
  public void ifProtectedDomainCodeSourceIsNotDefined_returnNull() {
    assertNull("String has code source", String.class.getProtectionDomain().getCodeSource());
    BootConfigLocator.locateFile(String.class, "rt.jar");
  }

  @Test
  public void findPropertyFirst() {
    File jar = getFileFromProtectionDomain(Test.class);
    System.setProperty(BootConfigLocator.JVM_CONFIG_OPTION, "./pom.xml");

    String modifiedFileName = replaceExtension(jar.getName(), "pom");

    File result = BootConfigLocator.locateFile(Test.class, modifiedFileName);
    System.clearProperty(BootConfigLocator.JVM_CONFIG_OPTION);
    assertEquals(new File(".", "pom.xml"), result);
  }

  private File getFileFromProtectionDomain(Class<?> targetClass) {
    return new File(targetClass.getProtectionDomain().getCodeSource().getLocation().getFile());
  }

  private String replaceExtension(String original, String newExtension) {
    return original.substring(0, original.lastIndexOf('.') + 1) + newExtension;
  }

  private Class<?> createClassLoaderAndLoadClass(File jarFile, String classToLoad) throws MalformedURLException, ClassNotFoundException {
    URL[] urls = {jarFile.toURI().toURL()};
    URLClassLoader classLoader = new URLClassLoader(urls, null);
    return classLoader.loadClass(classToLoad);
  }

  private File copyClassJarFileToDestination(Class<Test> aClass, String target) throws IOException {
    File srcFile = getJarFileFromClass(aClass);
    File destFile = new File(target, srcFile.getName());
    FileUtils.copyFile(srcFile, destFile);
    assertTrue(destFile + " was note copied", destFile.exists());
    return destFile;
  }

  private File getJarFileFromClass(Class<?> aClass) throws UnsupportedEncodingException {
    return new File(URLDecoder.decode(aClass.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
  }

}