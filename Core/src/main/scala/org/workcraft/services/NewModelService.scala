package org.workcraft.services

import org.workcraft.scala.effects.IO

object NewModelService extends Service[GlobalScope, NewModelImpl]

trait NewModelImpl {
  def name: String
  def create: IO[Model]
}