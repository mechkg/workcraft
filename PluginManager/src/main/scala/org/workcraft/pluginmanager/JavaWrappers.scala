package org.workcraft.pluginmanager
import scalaz.effects.IO
import scalaz.effects.IO._
import scalaz.Scalaz._
import java.io.FilenameFilter

object JavaWrappers {
  object System {
    def getProperty (s : String) : IO[String] = java.lang.System.getProperty(s).pure
    
  }
  class File(val f : java.io.File) {
    def isDirectory : IO[Boolean] = f.isDirectory.pure
    def isFile : IO[Boolean] = f.isFile.pure
    def getName : String = f.getName
    def getPath : String = f.getPath
    def listFiles (filter : FilenameFilter) : IO[List[File]] = f.listFiles(filter).pure.map(arr => arr.toList.map(f => new File(f)))
  }
  object File {
    def separator : String = java.io.File.separator
    def apply(name : String) = new File(new java.io.File(name))
    def separatorChar : Char = java.io.File.separatorChar
  }
}
