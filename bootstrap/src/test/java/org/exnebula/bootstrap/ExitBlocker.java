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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;

public class ExitBlocker {

  private static SecurityManager securityManager = null;

  public static int runMainAndCaptureExit(Class<?> aClass, String[] args) {
    try {
      Method main = aClass.getMethod("main", String[].class);
      main.invoke(null, new Object[]{args});
    } catch (InvocationTargetException e) {
      if(e.getTargetException() instanceof ExitException) {
        return ((ExitException)e.getTargetException()).status;
      } else {
        throw new RuntimeException("Execute main failed: ", e);
      }
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Execute main failed: ", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Execute main failed: ", e);
    }
    return 0;
  }

  public static void captureExit() {
    securityManager = System.getSecurityManager();
    System.setSecurityManager(new ExitBlocker.NoExitSecurityManager());
  }

  public static void releaseExit() {
    System.setSecurityManager(securityManager);
  }

  static class ExitException extends SecurityException {
    public final int status;

    public ExitException(int status) {
      super("Exited with status: " + status);
      this.status = status;
    }
  }

  static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // must be here to check authorize
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
      // must be here to check authorize
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }
}