package org.workcraft.logging
import java.text.SimpleDateFormat
import java.util.Date
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._

class StandardStreamLogger extends Logger[IO] {
  val DEBUG   = "   DEBUG| "
  val INFO    = "    INFO| "
  val WARNING = " WARNING| "
  val ERROR   = "   ERROR| "                
  val DUMMY   = "                           | " 
  
  val dateFormatString = "dd.MM.yyyy HH:mm:ss"
  val dateFormat = new SimpleDateFormat(dateFormatString)
  
  private def format (message: String, prefix: String) = 
    dateFormat.format(new Date()) + prefix + message
    
  private def print (stream: java.io.PrintStream, message:String, prefix: String) = {
    val lines = message.split("\n").toList
    stream.println (format(lines.head, prefix))
    lines.tail.foreach( s => stream.println (DUMMY + s))
  }
  
  def log (message: String, klass: MessageClass) = klass match {
    case MessageClass.Debug => print (System.out, message, DEBUG).pure
    case MessageClass.Info => print (System.out, message, INFO).pure
    case MessageClass.Warning => print (System.err, message, WARNING).pure
    case MessageClass.Error => print (System.err, message, ERROR).pure
  } 
}