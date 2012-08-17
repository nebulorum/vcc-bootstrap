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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class BootConfigLoaderTest {

  private BootConfigLoader loader;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    loader = new BootConfigLoader();
  }

  @Test
  public void havingEntryPointOnlyIsNotEnough() throws Exception {
    expectInvalidConfigurationException("Must have at least one 'cp' entry");
    BootConfig config = loadConfigFromString("ep=vcc.start.Main\n");
    assertNull(config);
  }

  @Test
  public void loadMinimalConfig() throws Exception {
    BootConfig config = loadConfigFromString("ep=vcc.start.Main\ncp=lib/lib.jar\n");
    assertEquals("vcc.start.Main", config.getEntryPoint());
    assertEquals("lib/lib.jar", config.getClassPath().get(0));
    assertEquals(1, config.getClassPath().size());
  }

  @Test(expected = IllegalStateException.class)
  public void canOnlyLoadOnce() throws IOException {
    loadConfigFromString("ep=vcc.start.Main\ncp=lib/lib.jar\n");
    loadConfigFromString("ep=vcc.start.Main\ncp=lib/lib.jar\n");
  }

  @Test
  public void acceptComments() throws Exception {
    BootConfig config = loadConfigFromString("# Simple Strap\n" +
      "ep=vcc.start.Main\n" +
      "# With\n# lib\n" +
      "cp=lib/myLib.jar");
    assertEquals("vcc.start.Main", config.getEntryPoint());
    assertEquals("lib/myLib.jar", config.getClassPath().get(0));
  }

  @Test
  public void haveAnEntryPointAndClassPath() throws Exception {
    BootConfig config = loadConfigFromString("ep=vcc.other.EP\ncp=lib/myLib.jar\n");
    assertEquals("vcc.other.EP", config.getEntryPoint());
    assertEquals("lib/myLib.jar", config.getClassPath().get(0));
  }

  @Test
  public void haveOnlyOneEntryPoint() throws Exception {
    expectInvalidConfigurationException("Must have exactly one 'ep' entry");
    BootConfig config = loadConfigFromString("ep=vcc.other.EP\nep=lib/myLib.jar\n");
    assertNull(config);
  }

  @Test
  public void haveMultipleClassPaths() throws Exception {
    BootConfig config = loadConfigFromString("ep=vcc.other.EP\ncp=lib/myLib.jar\ncp=lib/other.jar");
    assertEquals("vcc.other.EP", config.getEntryPoint());
    assertEquals("lib/myLib.jar", config.getClassPath().get(0));
    assertEquals("lib/other.jar", config.getClassPath().get(1));
  }

  @Test
  public void ignoreOrderOfDirectives() throws Exception {
    BootConfig config = loadConfigFromString("cp=lib/myLib.jar\nep=vcc.other.EP\ncp=lib/other.jar");
    assertEquals("vcc.other.EP", config.getEntryPoint());
    assertEquals("lib/myLib.jar", config.getClassPath().get(0));
    assertEquals("lib/other.jar", config.getClassPath().get(1));
  }

  @Test
  public void rejectEmptyFile() throws IOException {
    expectInvalidConfigurationException("Must have exactly one 'ep' entry");
    loadConfigFromString("# Empty\n");
  }

  @Test
  public void rejectEmptyFile2() throws IOException {
    expectInvalidConfigurationException("Must have exactly one 'ep' entry");
    assertNull(loadInNewLoader("# hey\n#Empty"));
  }

  @Test
  public void rejectFileWithWrongData() throws Exception {
    String badString = "Hello";
    expectInvalidConfigurationException("Illegal line '" + badString + "'");
    assertNull(loadConfigFromString("ep=one\n" + badString));
  }

  private void expectInvalidConfigurationException(String message) {
    expectedException.expect(BootConfigLoader.InvalidConfigurationException.class);
    expectedException.expectMessage(message);
  }

  private BootConfig loadInNewLoader(String content) throws IOException {
    InputStream is = new ByteArrayInputStream(content.getBytes());
    BootConfigLoader loader = new BootConfigLoader();
    loader.load(is);
    return loader.getConfiguration();
  }

  private BootConfig loadConfigFromString(String content) throws IOException {
    InputStream is = new ByteArrayInputStream(content.getBytes());
    loader.load(is);
    return loader.getConfiguration();
  }
}