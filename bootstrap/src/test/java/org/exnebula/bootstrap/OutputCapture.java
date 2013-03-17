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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OutputCapture {

  private final ByteArrayOutputStream capturedOutput;
  private final ByteArrayOutputStream capturedError;

  public OutputCapture() {
    capturedOutput = new ByteArrayOutputStream(1000);
    capturedError = new ByteArrayOutputStream(1000);
    System.setOut(new PrintStream(capturedOutput));
    System.setErr(new PrintStream(capturedError));
  }

  public String[] getOutput() {
    return capturedOutput.toString().split(System.getProperty("line.separator"));
  }

  public String[] getError() {
    return capturedError.toString().split(System.getProperty("line.separator"));
  }

}