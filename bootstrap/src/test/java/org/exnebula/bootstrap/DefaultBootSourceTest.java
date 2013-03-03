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

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.exnebula.bootstrap.TestHelper.getConfigFileAssociatedWithBoot;
import static org.exnebula.bootstrap.TestHelper.makeConfigFile;
import static org.junit.Assert.*;

public class DefaultBootSourceTest {

  @Test
  public void whenConfigDoesNotExist_returnNull() {
    File config = getConfigFileAssociatedWithBoot();
    config.delete();
    assertFalse("Previous config is not there", config.exists());
    assertNull("No config file", new DefaultBootInputSource().getConfigInputStream());
  }

  @Test
  public void whenConfigExist_returnStream() throws IOException {
    File config = getConfigFileAssociatedWithBoot();
    makeConfigFile("sample.Hello", "some.jar");
    assertNotNull("Got an input stream", new DefaultBootInputSource().getConfigInputStream());
    config.delete();
  }

}
