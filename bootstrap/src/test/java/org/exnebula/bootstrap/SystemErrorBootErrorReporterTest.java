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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SystemErrorBootErrorReporterTest {

  static private final String EOL = System.getProperty("line.separator");

  @Before
  public void setUp() {
    ExitBlocker.captureExit();
  }

  @After
  public void tearDown() {
    ExitBlocker.releaseExit();
  }

  @Test
  public void sendReportToSystemError() {
    ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream(1000);
    ByteArrayOutputStream capturedError = new ByteArrayOutputStream(1000);
    System.setOut(new PrintStream(capturedOutput));
    System.setErr(new PrintStream(capturedError));

    BootErrorReporter reporter = new SystemErrorBootErrorReporter();
    try {
      reporter.reportFailure("some context", new RuntimeException("The error"));
      fail("Did not exit JVM");
    } catch (ExitBlocker.ExitException e) {
      assertEquals("Send correct status", 1, e.status);
    }

    String[] lines = capturedError.toString().split(EOL);
    assertTrue("output has many lines", lines.length > 0);
    assertEquals("Description not in first line", "some context: The error", lines[0]);
    assertEquals("Stack trace start on second line", "java.lang.RuntimeException: The error", lines[1]);
    assertTrue("Stack trace continues", lines[2].startsWith("\tat " + this.getClass().getName()));
    assertEquals("stdout is not empty", "", capturedOutput.toString());
  }
}