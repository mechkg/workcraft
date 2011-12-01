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

import java.io.File
import java.io.InputStream
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.exceptions.DeserialisationException
import org.workcraft.exceptions.FormatException
import org.workcraft.interop.Importer
import org.workcraft.interop.ModelServices
import org.workcraft.interop.ServiceProvider
import org.workcraft.plugins.stg.HistoryPreservingStorageManager
import org.workcraft.plugins.stg.javacc.generated.DotGParser
import org.workcraft.plugins.stg.javacc.generated.ParseException
import org.workcraft.plugins.stg21.parsing.ParserHelper
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.plugins.stg21.StgModelDescriptor
import org.workcraft.plugins.stg21.types.VisualStg
import org.workcraft.plugins.stg21.types.VisualModel

object DotGImporter extends Importer {
	
	override def accept(file : File) : Boolean = {
		file.getName().endsWith(".g");
	}

	override val getDescription= "Signal Transition Graph (.g)"
	  
	@throws (classOf[DeserialisationException])
	override def importFrom(in : InputStream) : ModelServices = {
	  StgModelDescriptor.newDocument(VisualStg(importStg(in),VisualModel.empty))
	}

	@throws (classOf[DeserialisationException])
	def importStg(in : InputStream) : MathStg = {
		try {
		    val helper : ParserHelper = new ParserHelper
		    new DotGParser(in).parse(helper)
			val  result : MathStg = helper.getStg
			return result;
		} catch {
		  case (e : FormatException) => throw new DeserialisationException(e); 
		  case (e : ParseException) => throw new DeserialisationException(e); 
		}
	}
}
