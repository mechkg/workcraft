package org.workcraft.plugins.petri21
import org.workcraft.dom.ModelDescriptor
import org.workcraft.interop.ModelServices
import org.workcraft.interop.ModelServicesImpl
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.dom.Model
import org.workcraft.exceptions.NotImplementedException

class PetriNetModelDescriptor extends ModelDescriptor {
  def getDisplayName = "Petri net"
  def newDocument = ModelServicesImpl.EMPTY
  def createServiceProvider (model: Model, storage: StorageManager) = 
    throw new NotImplementedException //ModelServicesImpl.EMPTY plus
}