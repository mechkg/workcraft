package org.workcraft.plugins.stg21
import org.workcraft.Framework
import org.workcraft.dom.ModelDescriptor
import org.workcraft.plugins.interop.DotGImporter
import org.workcraft.interop.Importer
import org.workcraft.interop.Exporter
import org.workcraft.plugins.interop.DotGExporter
import org.workcraft.services.GlobalServiceProvider
import org.workcraft.services.Service
import org.workcraft.services.GlobalScope
import org.workcraft.services.NewModelService
import org.workcraft.services.NewModelImpl
import org.workcraft.services.Module

class StgModule extends Module {
  def name = "Signal Transition Graphs"
  def serviceProvider = StgServiceProvider
}

object StgServiceProvider extends GlobalServiceProvider {
  def implementations[T](service: Service[GlobalScope, T]) = service match {
    case NewModelService => List(NewStg)
    case _ => Nil
  }
}

object NewStg extends NewModelImpl {
  def name = "Signal Transition Graph"
  def create = StgModel.create
}

/*class StgModule extends Module {
	val getDescription : String = "Signal Transition Graphs"
	def init(framework : Framework) {
	  framework.getPluginManager().registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, StgModelDescriptor)
	  framework.getPluginManager().registerClass(Importer.SERVICE_HANDLE, DotGImporter)
	  framework.getPluginManager().registerClass(Exporter.SERVICE_HANDLE, DotGExporter)
	}
}
*/