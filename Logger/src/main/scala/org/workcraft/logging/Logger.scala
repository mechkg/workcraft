package org.workcraft.logging

trait Logger[M[_]] {
  def debug (message: String) : M[Unit]
  def info (message: String) : M[Unit]
  def warning (message: String) : M[Unit]
  def error (message: String) : M[Unit]
}

object Logger {
  def debug[M[_]] (message: String)(implicit logger:Logger[M]) : M[Unit] = logger.debug(message)
  def info[M[_]] (message: String)(implicit logger:Logger[M]) : M[Unit] = logger.info(message)
  def warning[M[_]] (message: String)(implicit logger:Logger[M]) : M[Unit] = logger.warning(message)
  def error[M[_]] (message: String)(implicit logger:Logger[M]) : M[Unit] = logger.error(message)
}