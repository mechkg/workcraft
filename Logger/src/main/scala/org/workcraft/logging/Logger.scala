package org.workcraft.logging

trait Logger {
  def debug (message: String)
  def info (message: String)
  def warning (message: String)
  def error (message: String)
}