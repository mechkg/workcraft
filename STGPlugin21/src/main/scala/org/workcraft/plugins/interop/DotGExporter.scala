/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 * 
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.interop

import java.io.IOException
import java.io.OutputStream

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.exceptions.ModelValidationException
import org.workcraft.exceptions.SerialisationException
import org.workcraft.interop.Exporter
import org.workcraft.interop.Exporter._
import org.workcraft.interop.ModelServices
import org.workcraft.interop.ServiceNotAvailableException
import org.workcraft.plugins.stg.serialisation.DotGSerialiser
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.serialisation.Format
import org.workcraft.dependencymanager.advanced.core.GlobalCache._

object DotGExporter extends Exporter {
	class ExportJob(model : Expression[MathStg]) extends org.workcraft.interop.ExportJob {

		override def getCompatibility : Int = BEST_COMPATIBILITY

		@throws(classOf[IOException])
		@throws(classOf[ModelValidationException])
		@throws(classOf[SerialisationException])
		override def export(out : OutputStream) = DotGSerialiser.serialise(eval(model), out)
	}
	override def getDescription : String = DotGSerialiser.getExtension + " (" + DotGSerialiser.getDescription + ")"

	override def getExtenstion : String = DotGSerialiser.getExtension

	override def getTargetFormat : Format = Format.STG
	
	@throws(classOf[ServiceNotAvailableException])
	override def getExportJob(modelServices : ModelServices) : ExportJob = {
		val model : Expression[MathStg] = modelServices.getImplementation(org.workcraft.plugins.stg21.types.MATH_STG_SERVICE_HANDLE)
		new ExportJob(model)
	}
}
