package org.workcraft.pluginmanager

import java.io.File
import java.io.FilenameFilter
import java.util.jar.JarFile
import java.util.Enumeration
import java.lang.reflect.Modifier
import java.net.URL

object PluginFinder {
  def isClass(file: File): Boolean = file.getName.endsWith(".class")
  def isJar(file: File): Boolean = file.getName.endsWith(".jar")

  val classFileFilter = new FilenameFilter {
    def accept(dir: File, name: String): Boolean = {
      val f = new File(dir.getPath + File.separator + name)
      f.isDirectory || isClass(f) || isJar(f)
    }
  }

  def searchClassPath(packages: List[String]) : Seq[Class[_]] = {
	 val classPathLocations = System.getProperty("java.class.path").split(System.getProperty("path.separator")).toSeq
	 
	 classPathLocations.flatMap (searchPath)
  }
  
  def searchPath (path: String) : Seq[Class[_]] = search (new File(path))

  def search(file: File): Seq[Class[_]] = {
    if (file.isDirectory)
      file.listFiles(classFileFilter).toSeq.flatMap(search)
    else if (file.isFile) {
      if (isClass(file)) {
        processClass(className(file.getPath()))
      } else if (isJar(file)) {
        val entries = new JarFile(file).entries()
        val seq = scala.collection.mutable.Seq()

        while (entries.hasMoreElements()) seq :+ className(entries.nextElement.getName)

        seq.flatMap(processClass)
      } else Nil
    } else Nil
  }

  def className(path: String) = {
    val name = if (path.startsWith(File.separator))
      path.substring(File.separator.length());
    else
      path

    name.replace(File.separatorChar, '.').replace('/', '.').substring(0, name.length() - ".class".length)
  }

  def processClass(className: String): Seq[Class[_]] = {
    try {
      val cls = Class.forName(className)

      if (!Modifier.isAbstract(cls.getModifiers()) && classOf[Plugin].isAssignableFrom(cls)) {
        val defCons = cls.getConstructor()
        Seq(cls)
      } else
        Nil

    } catch {
      case e: NoSuchMethodException =>
        {
          System.err.println("Plugin " + className + " does not have a default constructor. skipping.")
          Nil
        }
      case e =>
        System.err.println("Bad class " + className + ": " + e.getMessage())
        Nil
    }
  }
}