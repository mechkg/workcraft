package org.workcraft.services

import org.workcraft.scala.effects.IO

object FileNameService extends Service[ModelScope, FileName]

trait FileName {
  def lastSavedAs: Option[String]
  def update (savedAs: String): IO[Unit]
}
