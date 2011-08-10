package org.workcraft.plugins.petri21

import org.workcraft.Module
import org.workcraft.Framework
import org.workcraft.dom.ModelDescriptor

class PetriNetModule extends Module {
  def getDescription = "Petri Net 2.1"
  def init(framework: Framework) = {
    val pluginManager = framework.getPluginManager
    
    pluginManager.registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, new PetriNetModelDescriptor())
  }
}