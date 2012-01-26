package org.workcraft.logging
import java.text.SimpleDateFormat
import java.util.Date
import scalaz.effects.IO
import scalaz.effects.IO._
import scalaz.Scalaz._

class StandardStreamLogger extends Logger[IO] {
  val DEBUG   = "   DEBUG| "
  val INFO    = "    INFO| "
  val WARNING = " WARNING| "
  val ERROR   = "   ERROR| "
  
  val dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
  
  def format (message: String, prefix: String) = 
    dateFormat.format(new Date()) + prefix + message
        
  def debug   (message: String) = {System.out.println (format (message, DEBUG))}.pure
  def info    (message: String) = {System.out.println (format (message, INFO))}.pure
  def warning (message: String) = {System.err.println (format (message, WARNING))}.pure
  def error   (message: String) = {System.err.println (format (message, ERROR))}.pure
}
