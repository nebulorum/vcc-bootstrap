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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BootConfig {
  private String entryPoint = "vcc.start.Main";
  private List<String> classPath;

  public BootConfig(String entryPoint, String[] classPath) {
    this.entryPoint = entryPoint;
    LinkedList<String> list = new LinkedList<String>();
    Collections.addAll(list, classPath);
    this.classPath = Collections.unmodifiableList(list);
  }


  public String getEntryPoint() {
    return entryPoint;
  }

  public List<String> getClassPath() {
    return classPath;
  }
}