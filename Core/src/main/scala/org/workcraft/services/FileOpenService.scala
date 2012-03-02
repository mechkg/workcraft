package org.workcraft.services
import java.io.File

object FileOpenService extends Service[GlobalScope, FileOpenImpl]

trait FileOpenImpl {
  def description: String
  def supports(file: File): Boolean
  def open(file: File): ModelServiceProvider
}
