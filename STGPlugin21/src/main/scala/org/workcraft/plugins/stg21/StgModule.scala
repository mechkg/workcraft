package org.workcraft.plugins.stg21
import org.workcraft.Module
import org.workcraft.Framework
import org.workcraft.dom.ModelDescriptor

class StgModule extends Module {
	val getDescription : String = "Signal Transition Graphs"
	def init(framework : Framework) {
	  framework.getPluginManager().registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, StgModelDescriptor)
	}
}
