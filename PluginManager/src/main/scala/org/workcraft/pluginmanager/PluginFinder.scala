package org.workcraft.pluginmanager

import java.io.File
import java.io.FilenameFilter
import java.util.jar.JarFile
import java.util.Enumeration
import java.lang.reflect.Modifier
import java.net.URL

class PluginFinder(val packages: Traversable[String]) {
  def isClass(file: File): Boolean = file.getName.endsWith(".class")
  def isJar(file: File): Boolean = file.getName.endsWith(".jar")

  val classFileFilter = new FilenameFilter {
    def accept(dir: File, name: String): Boolean = {
      val f = new File(dir.getPath + File.separator + name)
      f.isDirectory || isClass(f) || isJar(f)
    }
  }

  /**
   * Takes a file system name and converts it into a class name provided that
   * it is contained in one of the packages whose names are passed to the
   * constructor.
   * @return Some string if the class name was successfully extracted or
   * None otherwise
   */
  def className(path: String): Option[String] = {
    val dotSeparatedName =
      if (path.endsWith(".class"))
        Some(path.replace(File.separatorChar, '.').replace('/', '.').substring(0, path.length() - ".class".length))
      else
        None

    for (
      n <- dotSeparatedName;
      pack <- packages.find(n.contains(_))
    ) yield n.substring(n.lastIndexOf(pack))
  }

  def searchClassPath(): Traversable[String] = {
    val classPathLocations = System.getProperty("java.class.path").split(System.getProperty("path.separator")).toList
    
    // duplicates may appear because the same location may be reachable
    // from several locations listed in the class path
    classPathLocations.flatMap(searchPath).distinct
  }

  def searchPath(path: String): Traversable[String] = search(new File(path))

  def search(file: File): Traversable[String] = {
    if (file.isDirectory)
      file.listFiles(classFileFilter).toList.flatMap(search)
    else if (file.isFile) {
      if (isClass(file)) {
        className(file.getPath())
      } else if (isJar(file)) {
        val entries = new JarFile(file).entries()
        val buffer = scala.collection.mutable.ListBuffer[Option[String]]()

        while (entries.hasMoreElements()) { buffer += className(entries.nextElement.getName) }

        buffer.toList.flatten
      } else Nil
    } else Nil
  }
}