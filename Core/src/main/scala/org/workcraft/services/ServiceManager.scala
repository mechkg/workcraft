package org.workcraft.services

class GlobalServiceManager (modules: Traversable[Module]) {
  val serviceProviders = modules.flatMap(_.services)
  
  def implementations[T] (service: Service[GlobalScope, T]) = serviceProviders.flatMap(_.implementation(service))
}