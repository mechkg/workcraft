package org.workcraft.services

object NewModelService extends Service[GlobalScope, NewModelImpl]

trait NewModelImpl {
  def name: String
  def create: Model
}