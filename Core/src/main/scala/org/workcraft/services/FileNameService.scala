package org.workcraft.services

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

object FileNameService extends Service[ModelScope, FileName] {
  def attach (model: ModelServiceProvider, fileName: Option[String]) = model ++ new ModelServiceProvider {
    def implementation[T](service: Service[ModelScope,T]): Option[T] = service match {
      case FileNameService => Some (new Object with FileName {
	var name = fileName
	def lastSavedAs = name
	def update (savedAs: String) = ioPure.pure { name = Some(savedAs) }
      })
      case _ => None
    }
  }
}

trait FileName {
  def lastSavedAs: Option[String]
  def update (savedAs: String): IO[Unit]
}
