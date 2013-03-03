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
package sample;

public class Good {
  private static boolean fired = false;

  public static void main(String[] args) {
    System.setProperty("sample.Good", makeExecutionMark(args));
    fired = true;
  }

  public static boolean hasMainSetStaticVariable() {
    return fired;
  }

  public static boolean hasSetSystemProperty(String[] args) {
    String comparator = makeExecutionMark(args);

    return comparator.equals(System.getProperty("sample.Good"));
  }

  private static String makeExecutionMark(String[] args) {
    String comparator = "wasCalled";
    for(String arg: args)  comparator += arg;
    return comparator;
  }
}