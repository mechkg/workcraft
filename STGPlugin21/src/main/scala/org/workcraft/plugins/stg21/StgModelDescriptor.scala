package org.workcraft.plugins.stg21

import org.workcraft.dom.ModelDescriptor
import org.workcraft.interop.ModelServices
import org.workcraft.interop.ModelServicesImpl
import org.workcraft.exceptions.NotSupportedException
import org.workcraft.dom.Model
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.gui.graph.GraphEditable
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.plugins.stg21.types.VisualStg
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.plugins.stg21.types.VisualModel
import org.workcraft.plugins.stg21.modifiable._
import org.workcraft.dependencymanager.advanced.core.Expression

/*object StgModelDescriptor extends ModelDescriptor {
	val getDisplayName : String = "Signal Transition Graph";
	def newDocument : ModelServices = newDocument(VisualStg.empty)
	def newDocument (stg : MathStg) : ModelServices = newDocument(VisualStg(stg, VisualModel.empty))
	def newDocument (stg : VisualStg) = {
	  val visualModel : ModifiableExpression[VisualStg] = Variable.create(stg)
	  ModelServicesImpl.EMPTY
	    .plus(GraphEditable.SERVICE_HANDLE, new StgGraphEditable(visualModel))
	    .plus[Expression[MathStg]](org.workcraft.plugins.stg21.types.MATH_STG_SERVICE_HANDLE, decorateModifiableVisualStg(visualModel).math)
	}
	def createServiceProvider(model : Model, storage : StorageManager) : ModelServices = { throw new NotSupportedException(); };
}
*/