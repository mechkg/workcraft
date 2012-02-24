package org.workcraft.services

object ModelDescriptorService extends Service[GlobalScope, ModelDescriptor]

trait ModelDescriptor {
  def modelName: String
  def newInstance: Model
}