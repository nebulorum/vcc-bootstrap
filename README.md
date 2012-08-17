VCC Maven Launcher
==================

This is part of a larger project Virtual Combat Cards (http://www.exnebula.org/vcc). This is an attempt to replace the
current launch and update mechanism with something that allows more frequent updates.

Since VCC is a Swing client Application and all artifact are built on Maven, the objective here is to have a launch
process that can check a Maven repository, get the updates, adjust the classpath, then launch the application.

To make all parts upgradeable the intent is to have the launch work as follows:

1. Start application (via MacOS Java Stub or Launch4J executable), this program will call the bootstrap module.
2. Bootstrap read a small file that tells it what other jar files to load and what is the next entry point. Because it
read a file you can replace the next step without much work.
3. The next point is the aether-launcher which will use Maven Aether embedded to go out to repositories and fetch the
 relevant artifacts, build the target classpath and launch the next phase: The application.

Because Aether can fetch version, detect missing files, etc, this should be a resilient process.
