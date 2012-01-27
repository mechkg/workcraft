package org.workcraft.logging
import scalaz.effects.IO

trait Logger[M[_]] {
  def debug (message: String) : M[Unit]
  def info (message: String) : M[Unit]
  def warning (message: String) : M[Unit]
  def error (message: String) : M[Unit]
}

object Logger {
  private def formatExceptionMessage (e: Throwable) = e.getClass().getName() + ": " + e.getMessage() + "\n" + e.getStackTraceString
  
  def debug[M[_]] (message: String)(implicit logger:Logger[M]) = logger.debug(message)
  def info[M[_]] (message: String)(implicit logger:Logger[M]) = logger.info(message)
  def warning[M[_]] (message: String)(implicit logger:Logger[M]) = logger.warning(message)
  def warning[M[_]] (exception: Throwable)(implicit logger:Logger[M]) = logger.warning(formatExceptionMessage(exception))
  def error[M[_]] (message: String)(implicit logger:Logger[M]) = logger.error(message)
  def error[M[_]] (exception: Throwable)(implicit logger:Logger[M]) = logger.error(formatExceptionMessage(exception))
  
  def unsafeDebug (message:String)(implicit logger:Logger[IO]) = logger.debug(message).unsafePerformIO
  def unsafeInfo (message:String)(implicit logger:Logger[IO]) = logger.info(message).unsafePerformIO
  def unsafeWarning (message:String)(implicit logger:Logger[IO]) = logger.warning(message).unsafePerformIO
  def unsafeError (message:String)(implicit logger:Logger[IO]) = logger.error(message).unsafePerformIO
}