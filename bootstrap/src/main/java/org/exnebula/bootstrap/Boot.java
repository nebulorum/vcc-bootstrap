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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Boot {
  private BootErrorReporter reporter;
  private BootInputSource inputSource;
  private String step = null;

  public Boot(BootErrorReporter reporter, BootInputSource inputSource) {
    this.reporter = reporter;
    this.inputSource = inputSource;
  }

  public void start() {
    try {
      startInner();
    } catch (Exception e) {
      reporter.reportFailure(step, e);
    }
  }

  private void startInner() throws Exception {
    InputStream stream = locateConfigOrFail();
    BootConfig config = loadConfigurationOrFail(stream);
    validaClassPathsOrFail(config);
    runMain(config);
  }

  private BootConfig loadConfigurationOrFail(InputStream stream) throws IOException {
    step = "Read config file";
    BootConfigLoader bootConfigLoader = new BootConfigLoader();
    bootConfigLoader.load(stream);
    return bootConfigLoader.getConfiguration();
  }

  private InputStream locateConfigOrFail() {
    step = "Locate config file";
    InputStream stream = inputSource.getConfigInputStream();
    if (stream == null)
      throw new BootStrapException("Could not locate configuration file");
    return stream;
  }

  private void validaClassPathsOrFail(BootConfig config) throws FileNotFoundException {
    step = "Check class path files";
    for (String path : config.getClassPath()) {
      if (!inputSource.fileExists(path)) {
        throw new FileNotFoundException("File " + path + " not found");
      }
    }
  }

  private void runMain(BootConfig config) throws Exception {
    step = "Start entry point";
    Class<?> aClass = makeClassLoader(config).loadClass(config.getEntryPoint());
    Method main = aClass.getMethod("main", String[].class);
    assertIsStaticMethod(aClass, main);
    main.invoke(null, new Object[]{new String[]{}});
  }

  private URLClassLoader makeClassLoader(BootConfig config) throws MalformedURLException {
    URL[] urls = mapFilesToURL(config);

    return new URLClassLoader(urls, this.getClass().getClassLoader().getParent());
  }

  private URL[] mapFilesToURL(BootConfig config) throws MalformedURLException {
    URL[] urls = new URL[config.getClassPath().size()];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = new File(config.getClassPath().get(i)).toURI().toURL();
    }
    return urls;
  }

  private void assertIsStaticMethod(Class<?> aClass, Method main) throws NoSuchMethodException {
    if (!Modifier.isStatic(main.getModifiers()))
      throw new NoSuchMethodException(aClass.getName() + ".main not static");
  }
}