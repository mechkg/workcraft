package org.workcraft.pluginmanager

import java.io.FilenameFilter
import java.util.jar.JarFile
import java.util.Enumeration
import java.lang.reflect.Modifier
import java.net.URL
import scalaz.effects.IO
import scalaz.effects.IO._
import JavaWrappers._
import scalaz.Scalaz._

class PluginFinder(val packages: Traversable[String]) {
  def isClass(file: File): Boolean = file.getName.endsWith(".class")
  def isJar(file: File): Boolean = file.getName.endsWith(".jar")

  val classFileFilter = new FilenameFilter {
    def accept(dir: File, name: String): Boolean = {
      val f = File(dir.getPath + File.separator + name)
      f.isDirectory.unsafePerformIO || isClass(f) || isJar(f)
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

  def searchClassPath(): IO[Traversable[String]] = {
    (System.getProperty("java.class.path") |@| 
     System.getProperty("path.separator")).apply(
        (path, separator) => path.split(separator).toList) >>=
    // duplicates may appear because the same location may be reachable
    // from several locations listed in the class path
    (_.traverse(searchPath).map(_.flatten.distinct))
  }

  def searchPath(path: String): IO[Traversable[String]] = search(File(path))

  def search(file: File): IO[Traversable[String]] = {
    file.isDirectory >>= {
      case true => file.listFiles(classFileFilter) >>= (_.traverse(search).map(_.flatten))
      case false => file.isFile >>= {
        case true => isClass(file) match {
          case true => (className(file.getPath) : Traversable[String]).pure
          case false => isJar(file) match {
            case true =>  ({
					        val entries = new JarFile(file.f).entries()
					        val buffer = scala.collection.mutable.ListBuffer[Option[String]]()
					
					        while (entries.hasMoreElements()) { buffer += className(entries.nextElement.getName) }
					
					        buffer.toList.flatten
					      } : Traversable[String]).pure
            case false => (Nil : Traversable[String]).pure
          }
        }
        case false => (Nil  : Traversable[String]).pure
      }
    }
  }
}