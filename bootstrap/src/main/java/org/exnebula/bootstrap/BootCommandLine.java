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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BootCommandLine {
  public static void main(String[] args) {
    Boot boot = new Boot(
      new BootErrorReporter() {
        public void reportFailure(String contextMessage, Exception exception) {
          System.err.println(contextMessage + ": " + exception.getMessage());
          exception.printStackTrace(System.err);
          System.exit(1);
        }
      },
      new BootInputSource() {
        public InputStream getConfigInputStream() {
          try {
            File configFile = BootConfigLocator.locateFile(this.getClass(), "boot.cfg");
            return (configFile != null) ? new FileInputStream(configFile) : null;
          } catch (FileNotFoundException e) {
            return null;
          }
        }
      }
    );
    boot.start();
  }
}