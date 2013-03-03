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

import org.junit.Before;
import org.junit.Test;
import sample.Good;
import sample.NoStaticMain;

import java.io.*;

import static org.exnebula.bootstrap.TestHelper.getTargetDirectory;
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
  private FileChecker checker;
  private final String[] emptyArguments = {};

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
    checker = mock(FileChecker.class);
    boot = new Boot(errorReporter, bis, checker);

    File testPath = getTargetDirectory();
    realTestClasses = new File(testPath, "test-classes").getPath();
    localPathThatDoesNotExist = new File(testPath, "not-found").getPath();

    when(checker.fileExists(jarOne)).thenReturn(true);
    when(checker.fileExists(missingJar)).thenReturn(false);
    when(checker.fileExists(jarTwo)).thenReturn(true);
    when(checker.fileExists(realTestClasses)).thenReturn(true);
    when(checker.fileExists(localPathThatDoesNotExist)).thenReturn(true);
  }

  @Test
  public void failGetStream() throws Exception {
    boot.start(emptyArguments);

    verify(bis, atLeastOnce()).getConfigInputStream();
    errorReporter.verifyCrashReportChained("Locate config file", BootStrapException.class, "Could not locate configuration file");
  }

  @Test
  public void gotValidStreamButBadConfigurationFile() throws Exception {
    setConfigStream("bad");

    boot.start(emptyArguments);

    verify(bis, atLeastOnce()).getConfigInputStream();
    errorReporter.verifyCrashReportChained("Read config file", BootConfigLoader.InvalidConfigurationException.class, "Illegal line 'bad'");
  }

  @Test
  public void gotStreamThatThrowsIOException() throws Exception {
    doReturn(makeStreamThatThrowsIOException()).when(bis).getConfigInputStream();

    boot.start(emptyArguments);
    verify(bis, atLeastOnce()).getConfigInputStream();
    errorReporter.verifyCrashReportChained("Read config file", IOException.class, "boom");
  }

  @Test
  public void gotGoodFileLoadButFailedToFindClass() throws Exception {
    setConfigStream(makeConfig("org.pack.obj", jarOne, missingJar, jarTwo));

    boot.start(emptyArguments);

    verify(checker, atLeastOnce()).fileExists(jarOne);
    verify(checker, atLeastOnce()).fileExists(missingJar);
    verify(checker, never()).fileExists(jarTwo);
    errorReporter.verifyCrashReportChained("Check class path files", FileNotFoundException.class, "File " + missingJar + " not found");
  }

  @Test
  public void allPathsAreFoundButClassNotInPath() throws Exception {
    setConfigStream(makeConfig("sample.Good", localPathThatDoesNotExist));

    boot.start(emptyArguments);
    errorReporter.verifyCrashReportChained("Start entry point", ClassNotFoundException.class, "sample.Good");
  }

  @Test
  public void addAllClassPath_firstDoesNotHaveClass() throws Exception {
    setConfigStream(makeConfig("sample.Good", localPathThatDoesNotExist, realTestClasses));
    boot.start(emptyArguments);
    assertTrue("Set System property", Good.hasSetSystemProperty(emptyArguments));
    assertFalse("Good class does not share loader", Good.hasMainSetStaticVariable());
  }

  @Test
  public void allWhenWellAndInstanceFiresInItsContext() throws Exception {
    setConfigStream(makeConfig("sample.Good", realTestClasses));

    String[] realArguments = {"some", "other" + System.currentTimeMillis()};
    boot.start(realArguments);
    errorReporter.throwIfErrorReported();
    assertTrue("Set System property", Good.hasSetSystemProperty(realArguments));
    assertFalse("Good class does not share loader", Good.hasMainSetStaticVariable());
  }

  @Test
  public void classExistsButDoesNotHaveMain() throws Exception {
    setConfigStream(makeConfig(sample.NoMain.class.getName(), realTestClasses));

    boot.start(emptyArguments);
    errorReporter.verifyCrashReportChained("Start entry point",
      NoSuchMethodException.class, "sample.NoMain.main([Ljava.lang.String;)");
  }

  @Test
  public void classExistsButHasNonStaticMain() throws Exception {
    setConfigStream(makeConfig(NoStaticMain.class.getName(), realTestClasses));

    boot.start(emptyArguments);
    errorReporter.verifyCrashReportChained("Start entry point",
      NoSuchMethodException.class, "sample.NoStaticMain.main not static");
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