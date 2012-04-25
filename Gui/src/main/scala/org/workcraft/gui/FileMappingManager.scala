package org.workcraft.gui

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import org.workcraft.services.ModelServiceProvider
import java.io.File

class FileMappingManager {
  private var fileMapping = Map[ModelServiceProvider, ModifiableExpression[Option[File]]]()

  def update (model: ModelServiceProvider, savedAs: Option[File]): IO[Unit] =
    if (fileMapping.contains(model))
      fileMapping(model) := savedAs
    else
      newVar(savedAs).map ( v => fileMapping += (model -> v))

  def lastSavedAs (model: ModelServiceProvider): Expression[Option[File]] = fileMapping(model)
 
  def remove (model: ModelServiceProvider) = fileMapping -= model
}
