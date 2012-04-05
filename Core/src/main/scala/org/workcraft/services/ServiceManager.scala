package org.workcraft.services
import org.workcraft.pluginmanager.PluginManager

class GlobalServiceManager (val modules: List[Module]) extends GlobalServiceProvider {
  val serviceProviders = modules.map(_.serviceProvider)
  
  def implementations[T] (service: Service[GlobalScope, T]) = serviceProviders.flatMap(_.implementations(service)).toList
}