/*
 * Copyright (C) 2013-2013 - Thomas Santana <tms@exnebula.org>
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.exnebula.bootstrap.TestHelper.getTargetDirectory;

@SuppressWarnings("ALL")
public class CommandLineTest {

  private static String EOL = System.getProperty("line.separator");
  private OutputCapture outputCapture;
  private SecurityManager securityManager;
  private File targetJar;

  @Before
  public void setUp() throws IOException {
    targetJar = new File(getTargetDirectory(), "test-mini.jar");
    TestHelper.makeMiniJar(targetJar, new File(getTargetDirectory(), "test-classes"), "sample/Hello.class");

    ExitBlocker.captureExit();
    outputCapture = new OutputCapture();
  }

  @After
  public void tearDown() {
    ExitBlocker.releaseExit();
    System.setSecurityManager(securityManager);
    TestHelper.getConfigFileAssociatedWithBoot().delete();
  }

  @Test
  public void withGoodConfiguration_callMainINConfig() throws IOException {
    TestHelper.makeConfigFile("sample.Hello", targetJar.getPath());
    assertEquals("Ran main", 0, ExitBlocker.runMainAndCaptureExit(BootCommandLine.class, new String[]{}));
    assertEquals("Hello", outputCapture.getOutput()[0]);
    TestHelper.getConfigFileAssociatedWithBoot().delete();
  }

  @Test
  public void testExitBadly() throws IOException {
    TestHelper.getConfigFileAssociatedWithBoot().delete();
    assertFalse("Config file exists", TestHelper.getConfigFileAssociatedWithBoot().exists());
    String failureMessage = "Locate config file: Could not locate configuration file";
    assertEquals("Ran but did not issue exit", 1, ExitBlocker.runMainAndCaptureExit(BootCommandLine.class, new String[]{failureMessage}));
    assertEquals("Not matching expected error report", failureMessage, outputCapture.getError()[0]);
  }

  @Test
  public void shouldPassArgumentsToMain() throws IOException {
    TestHelper.makeConfigFile("sample.Hello", targetJar.getPath());
    String time = "time:" + System.currentTimeMillis();
    String[] args = {"fred", time};
    assertEquals("Ran main", 0, ExitBlocker.runMainAndCaptureExit(BootCommandLine.class, args));
    assertEquals("Hello fred! " + time + "!", outputCapture.getOutput()[0]);
    TestHelper.getConfigFileAssociatedWithBoot().delete();
  }

}