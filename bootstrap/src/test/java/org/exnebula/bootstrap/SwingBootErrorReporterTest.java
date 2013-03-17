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

import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class SwingBootErrorReporterTest extends UISpecTestCase {

  private final String contextMessage = "Huston we have a problem";
  private OutputCapture outputCapture;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ExitBlocker.captureExit();
    outputCapture = new OutputCapture();
  }

  public void testGettingMessage() {
    final SwingBootErrorReporter reporter = new SwingBootErrorReporter();
    WindowInterceptor.init(new Trigger() {
      public void run() throws Exception {
        ExitBlocker.runAndCaptureExit(new Runnable() {
          public void run() {
            reporter.reportFailure(contextMessage, new Exception("Boom!"));
          }
        });
        assertEquals("Exit code should be 1", 1 , ExitBlocker.getExitCode());
      }
    }).process("Startup failed", new WindowHandler() {
      @Override
      public Trigger process(Window window) throws Exception {
        JLabel label = window.findSwingComponent(JLabel.class, contextMessage);
        assertNotNull(label);
        assertEquals(contextMessage, label.getText());
        return window.getButton().triggerClick();
      }
    }).run();
    String[] errorLines = outputCapture.getError();
    assertEquals("Huston we have a problem: Boom!", errorLines[0]);
    assertEquals("java.lang.Exception: Boom!", errorLines[1]);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    ExitBlocker.releaseExit();
  }
}