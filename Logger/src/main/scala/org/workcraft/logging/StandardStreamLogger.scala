package org.workcraft.logging
import java.text.SimpleDateFormat
import java.util.Date

class StandardStreamLogger extends Logger {
  val DEBUG   = "   DEBUG| "
  val INFO    = "    INFO| "
  val WARNING = " WARNING| "
  val ERROR   = "   ERROR| "
  
  val dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
  
  def format (message: String, prefix: String) = 
    dateFormat.format(new Date()) + prefix + message
        
  def debug   (message: String) = System.out.println (format (message, DEBUG))
  def info    (message: String) = System.out.println (format (message, INFO))
  def warning (message: String) = System.err.println (format (message, WARNING))
  def error   (message: String) = System.err.println (format (message, ERROR))
}