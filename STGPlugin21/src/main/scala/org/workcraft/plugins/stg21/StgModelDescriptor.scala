package org.workcraft.plugins.stg21
import org.workcraft.dom.ModelDescriptor
import org.workcraft.interop.ModelServices
import org.workcraft.interop.ModelServicesImpl
import org.workcraft.exceptions.NotSupportedException
import org.workcraft.dom.Model
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.gui.graph.GraphEditable

object StgModelDescriptor extends ModelDescriptor {
	val getDisplayName : String = "Signal Transition Graph";
	def newDocument : ModelServices = {
	  ModelServicesImpl.EMPTY.plus(GraphEditable.SERVICE_HANDLE, new StgGraphEditable())
	}
	def createServiceProvider(model : Model, storage : StorageManager) : ModelServices = { throw new NotSupportedException(); };
}
