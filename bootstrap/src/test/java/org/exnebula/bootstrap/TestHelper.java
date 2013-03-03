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

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class TestHelper {

  static public File getTargetDirectory() {
    File base = new File("bootstrap");
    if (base.exists() && base.isDirectory()) {
      return new File(base, "target");
    }
    return new File("target");
  }

  public static void makeMiniJar(File targetJar, File baseDirectory, String classFile) throws IOException {
    JarOutputStream jar = new JarOutputStream(new FileOutputStream(targetJar));
    jar.putNextEntry(new ZipEntry(classFile));
    byte[] buffer = new byte[4 * 1024];
    InputStream in = new FileInputStream(new File(baseDirectory, classFile));
    int count;
    while ((count = in.read(buffer)) > 0) {
      jar.write(buffer, 0, count);
    }
    jar.close();
  }

  public static File getConfigFileAssociatedWithBoot() {
    return new File(getTargetDirectory(), BootConfigLocator.BOOT_FILE);
  }

  public static void makeConfigFile(String endpoint, String targetJarPath) throws IOException {
    FileUtils.writeLines(getConfigFileAssociatedWithBoot(),
      Arrays.asList(
        "ep=" + endpoint,
        "cp=" + targetJarPath));
  }
}