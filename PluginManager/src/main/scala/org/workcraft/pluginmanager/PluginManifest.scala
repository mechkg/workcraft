package org.workcraft.pluginmanager
import java.util.UUID
import java.io.PrintWriter
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileInputStream
import java.nio.charset.Charset
import scala.collection.mutable.ListBuffer

sealed trait ManifestReadError

object ManifestReadError {
  case class Empty() extends ManifestReadError
  case class VersionMismatch() extends ManifestReadError
  case class Exception(e: Throwable) extends ManifestReadError
}

object PluginManifest {
  def write(version: UUID, path: String, plugins: Traversable[String]) : Option[Throwable] = {
    try {
      val writer = new PrintWriter(path, "UTF-8")

      writer.println(version.toString())

      plugins.foreach(writer.println(_))

      writer.close()
      
      None
    } catch {
      case e => Some (e)
    }
  }

  def read(version: UUID, path: String): Either[ManifestReadError, List[String]] = {
    def readWith(reader: BufferedReader): Either[ManifestReadError, List[String]] = {
      def readList: List[String] = {
        val buffer = ListBuffer[String]()
        while (true) {
          val s = reader.readLine()
          if (s == null) return buffer.toList
          buffer += s
        }
        buffer.toList
      }
      try {
        val manifestString = reader.readLine()

        if (manifestString == null)
          Left(ManifestReadError.Empty())
        else if (!UUID.fromString(manifestString).equals(version))
          Left(ManifestReadError.VersionMismatch())
        else Right(readList)
      } catch {
        case e => Left(ManifestReadError.Exception(e))
      }
    }
    try {
      val reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), Charset.forName("UTF-8")))
      val result = readWith(reader)
      reader.close()
      result
    } catch {
      case e => Left(ManifestReadError.Exception(e))
    }
  }
}
