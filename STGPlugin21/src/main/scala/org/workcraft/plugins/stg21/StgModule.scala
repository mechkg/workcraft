package org.workcraft.plugins.stg21
import org.workcraft.Module
import org.workcraft.Framework
import org.workcraft.dom.ModelDescriptor
import org.workcraft.plugins.interop.DotGImporter
import org.workcraft.interop.Importer

class StgModule extends Module {
	val getDescription : String = "Signal Transition Graphs"
	def init(framework : Framework) {
	  framework.getPluginManager().registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, StgModelDescriptor)
	  framework.getPluginManager().registerClass(Importer.SERVICE_HANDLE, DotGImporter)
	}
}
