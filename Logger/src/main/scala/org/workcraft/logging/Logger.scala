package org.workcraft.logging
import org.workcraft.scala.effects.IO

sealed trait MessageClass 

object MessageClass {
  case object Info extends MessageClass
  case object Debug extends MessageClass
  case object Warning extends MessageClass
  case object Error extends MessageClass
}

trait Logger[M[_]] {
  def log (message: String, klass: MessageClass) : M[Unit]
}

object Logger {
  private def formatExceptionMessage (e: Throwable) = e.getClass().getName() + ": " + e.getMessage() + "\n" + e.getStackTraceString
  
  def debug[M[_]] (message: String)(implicit logger:() => Logger[M]) = logger().log(message, MessageClass.Debug)
  def info[M[_]] (message: String)(implicit logger:() => Logger[M]) = logger().log(message, MessageClass.Info)
  def warning[M[_]] (message: String)(implicit logger:() => Logger[M]) = logger().log(message, MessageClass.Warning)
  def warning[M[_]] (exception: Throwable)(implicit logger:() => Logger[M]) = logger().log(formatExceptionMessage(exception), MessageClass.Warning)
  def error[M[_]] (message: String)(implicit logger:() => Logger[M]) = logger().log(message, MessageClass.Error)
  def error[M[_]] (exception: Throwable)(implicit logger:() => Logger[M]) = logger().log(formatExceptionMessage(exception), MessageClass.Error)
  
  def unsafeDebug (message:String)(implicit logger:() => Logger[IO]) = debug(message).unsafePerformIO
  def unsafeInfo (message:String)(implicit logger:() => Logger[IO]) = info(message).unsafePerformIO
  def unsafeWarning (message:String)(implicit logger:() => Logger[IO]) = warning(message).unsafePerformIO
  def unsafeError (message:String)(implicit logger:() => Logger[IO]) = error(message).unsafePerformIO
}