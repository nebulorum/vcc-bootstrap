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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

@SuppressWarnings("ALL")
public class CommandLineTest {

  private static String EOL = System.getProperty("line.separator");
  private SecurityManager securityManager;
  private ByteArrayOutputStream capturedError;
  private File targetJar;
  private ByteArrayOutputStream capturedOutput;

  @Before
  public void setUp() throws IOException {
    targetJar = new File(getTargetDirectory(), "test-mini.jar");
    makeMiniJar(targetJar, new File(getTargetDirectory(), "test-classes"), "sample/Hello.class");

    ExitBlocker.captureExit();

    capturedOutput = new ByteArrayOutputStream(1000);
    capturedError = new ByteArrayOutputStream(1000);
    System.setOut(new PrintStream(capturedOutput));
    System.setErr(new PrintStream(capturedError));
  }

  @After
  public void tearDown() {
    ExitBlocker.releaseExit();
    System.setSecurityManager(securityManager);
    getConfigFile().delete();
  }

  @Test
  public void withGoodConfiguration_callMainINConfig() throws IOException {
    makeConfigFile("sample.Hello", targetJar.getPath());
    assertEquals("Ran main", 0, ExitBlocker.runMainAndCaptureExit(BootCommandLine.class, new String[]{}));
    assertEquals("Hello" + EOL, capturedOutput.toString());
    getConfigFile().delete();
  }

  @Test
  public void testExitBadly() throws IOException {
    getConfigFile().delete();
    assertFalse("Config file exists", getConfigFile().exists());
    String failureMessage = "Locate config file: Could not locate configuration file";
    assertEquals("Ran but did not issue exit", 1, ExitBlocker.runMainAndCaptureExit(BootCommandLine.class, new String[]{failureMessage}));
    assertEquals("Not matching expected error report", failureMessage, capturedError.toString().split(EOL)[0]);
  }

  private File getTargetDirectory() {
    File base = new File("bootstrap");
    if (base.exists() && base.isDirectory()) {
      return new File(base, "target");
    }
    return new File("target");
  }

  private void makeMiniJar(File targetJar, File baseDirectory, String classFile) throws IOException {
    JarOutputStream jar = new JarOutputStream(new FileOutputStream(targetJar));
    jar.putNextEntry(new ZipEntry(classFile));
    byte[] buffer = new byte[4 * 1024];
    InputStream in = new FileInputStream(new File(baseDirectory, classFile));
    int count;
    while ((count = in.read(buffer)) > 0) {
      jar.write(buffer, 0, count);
    }
    jar.close();
  }

  private void makeConfigFile(String endpoint, String targetJarPath) throws IOException {
    FileUtils.writeLines(getConfigFile(),
      Arrays.asList(
        "ep=" + endpoint,
        "cp=" + targetJarPath));
  }

  private File getConfigFile() {
    return new File(getTargetDirectory(), "boot.cfg");
  }
}