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
import java.io.IOException;

public class BootSwingTest extends UISpecTestCase {

  private OutputCapture outputCapture;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ExitBlocker.captureExit();
    outputCapture = new OutputCapture();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    ExitBlocker.releaseExit();
  }

  public void testWhenFailed_popupWarningAndExit() throws IOException {
    TestHelper.getConfigFileAssociatedWithBoot().delete();
    assertFalse("Config file exists", TestHelper.getConfigFileAssociatedWithBoot().exists());
    final String failureMessage = "Locate config file: Could not locate configuration file";
    WindowInterceptor.init(new Trigger() {
      public void run() throws Exception {
        ExitBlocker.runMainAndCaptureExit(BootSwing.class, new String[]{failureMessage});
      }
    }).process("Startup failed", new WindowHandler() {
      @Override
      public Trigger process(Window window) throws Exception {
        return window.getButton().triggerClick();
      }
    }).run();
    assertEquals("Ran but did not issue exit", 1, ExitBlocker.getExitCode());
    assertEquals("Not matching expected error report", failureMessage, outputCapture.getError()[0]);
  }

}