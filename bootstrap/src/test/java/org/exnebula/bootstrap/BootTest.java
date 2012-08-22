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
import org.junit.Test;
import sample.Good;
import sample.NoStaticMain;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BootTest {

  private TestBootErrorReporter errorReporter;
  private BootInputSource bis;
  private Boot boot;
  private final String jarOne = "lib/one.jar";
  private final String jarTwo = "lib/two.jar";
  private final String missingJar = "lib/missing.jar";
  private String realTestClasses;
  private String localPathThatDoesNotExist;

  private class TestBootErrorReporter implements BootErrorReporter {
    private Exception thrownException = null;

    public void reportFailure(String contextMessage, Exception exception) {
      thrownException = new BootStrapException(contextMessage, exception);
    }

    public Class<?> reportedExceptionClass() {
      return thrownException.getClass();
    }

    public void verifyCrashReportChained(String contextMessage, Class<? extends Throwable> chainedException, String message) {
      assertTrue("Should report error", thrownException != null);
      assertEquals(BootStrapException.class, reportedExceptionClass());
      assertEquals(chainedException, thrownException.getCause().getClass());
      assertEquals(contextMessage, thrownException.getMessage());
      assertEquals(message, thrownException.getCause().getMessage());
    }

    public void throwIfErrorReported() throws Exception {
      if (thrownException != null)
        throw thrownException;
    }
  }

  @Before
  public void setUp() throws Exception {
    errorReporter = new TestBootErrorReporter();
    bis = mock(BootInputSource.class);
    boot = new Boot(errorReporter, bis);

    String testPath = normalizeTestPath();
    realTestClasses = testPath + "test-classes";
    localPathThatDoesNotExist = testPath + "not-found";

    when(bis.fileExists(jarOne)).thenReturn(true);
    when(bis.fileExists(missingJar)).thenReturn(false);
    when(bis.fileExists(jarTwo)).thenReturn(true);
    when(bis.fileExists(realTestClasses)).thenReturn(true);
    when(bis.fileExists(localPathThatDoesNotExist)).thenReturn(true);
  }

  @Test
  public void failGetStream() throws Exception {
    boot.start();

    verify(bis, atLeastOnce()).getConfigInputStream();
    errorReporter.verifyCrashReportChained("Locate config file", BootStrapException.class, "Could not locate configuration file");
  }

  @Test
  public void gotValidStreamButBadConfigurationFile() throws Exception {
    setConfigStream("bad");

    boot.start();

    verify(bis, atLeastOnce()).getConfigInputStream();
    errorReporter.verifyCrashReportChained("Read config file", BootConfigLoader.InvalidConfigurationException.class, "Illegal line 'bad'");
  }

  @Test
  public void gotStreamThatThrowsIOException() throws Exception {
    doReturn(makeStreamThatThrowsIOException()).when(bis).getConfigInputStream();

    boot.start();
    verify(bis, atLeastOnce()).getConfigInputStream();
    errorReporter.verifyCrashReportChained("Read config file", IOException.class, "boom");
  }

  @Test
  public void gotGoodFileLoadButFailedToFindClass() throws Exception {
    setConfigStream(makeConfig("org.pack.obj", jarOne, missingJar, jarTwo));

    boot.start();

    verify(bis, atLeastOnce()).fileExists(jarOne);
    verify(bis, atLeastOnce()).fileExists(missingJar);
    verify(bis, never()).fileExists(jarTwo);
    errorReporter.verifyCrashReportChained("Check class path files", FileNotFoundException.class, "File " + missingJar + " not found");
  }

  @Test
  public void allPathsAreFoundButClassNotInPath() throws Exception {
    setConfigStream(makeConfig("sample.Good", localPathThatDoesNotExist));

    boot.start();
    errorReporter.verifyCrashReportChained("Start entry point", ClassNotFoundException.class, "sample.Good");
  }

  @Test
  public void addAllClassPath_firstDoesNotHaveClass() throws Exception {
    setConfigStream(makeConfig("sample.Good", localPathThatDoesNotExist, realTestClasses));

    boot.start();
    assertTrue("Set System property", Good.hasSetSystemProperty());
    assertFalse("Good class does not share loader", Good.hasMainSetStaticVariable());
  }

  @Test
  public void allWhenWellAndInstanceFiresInItsContext() throws Exception {
    setConfigStream(makeConfig("sample.Good", realTestClasses));

    boot.start();
    errorReporter.throwIfErrorReported();
    assertTrue("Set System property", Good.hasSetSystemProperty());
    assertFalse("Good class does not share loader", Good.hasMainSetStaticVariable());
  }

  @Test
  public void classExistsButDoesNotHaveMain() throws Exception {
    System.out.println("Name: " + sample.NoMain.class.getName());
    setConfigStream(makeConfig(sample.NoMain.class.getName(), realTestClasses));

    boot.start();
    errorReporter.verifyCrashReportChained("Start entry point",
      NoSuchMethodException.class, "sample.NoMain.main([Ljava.lang.String;)");
  }

  @Test
  public void classExistsButHasNonStaticMain() throws Exception {
    System.out.println("Name: " + sample.NoMain.class.getName());
    setConfigStream(makeConfig(NoStaticMain.class.getName(), realTestClasses));

    boot.start();
    errorReporter.verifyCrashReportChained("Start entry point",
      NoSuchMethodException.class, "sample.NoStaticMain.main not static");
  }

  private String normalizeTestPath() {
    String path = "";
    if (new File("bootstrap").exists())
      path = "bootstrap/";
    return path + "target/";
  }

  private InputStream makeStreamThatThrowsIOException() {
    return new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("boom");
      }
    };
  }

  private void setConfigStream(String inputContent) throws UnsupportedEncodingException {
    when(bis.getConfigInputStream()).thenReturn(streamFromString(inputContent));
  }

  private InputStream streamFromString(String inputContent) throws UnsupportedEncodingException {
    return new ByteArrayInputStream(inputContent.getBytes("UTF-8"));
  }

  private String makeConfig(String entryPoint, String... classpath) {
    String result = String.format("ep=%s\n", entryPoint);
    for (String path : classpath) {
      result += String.format("cp=%s\n", path);
    }
    return result;
  }
}