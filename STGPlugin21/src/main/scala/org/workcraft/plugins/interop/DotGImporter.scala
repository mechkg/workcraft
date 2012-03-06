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

import org.workcraft.plugins.stg.javacc.generated.DotGParser
import org.workcraft.plugins.stg21.parsing.ParserHelper
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.plugins.stg21.types.VisualModel
import org.workcraft.plugins.stg21.types.VisualStg
import org.workcraft.plugins.stg21.StgModel
import org.workcraft.scala.effects.IO.ioPure
import org.workcraft.scala.effects.IO
import org.workcraft.services.Model
import org.workcraft.services.Importer

import scalaz.Scalaz._

object DotGImporter extends Importer {
  override def accept(file: File): IO[Boolean] = {
    file.getName().endsWith(".g").pure[IO]
  }

  override val description = "Signal Transition Graph (.g)"

  override def importFrom(in: InputStream): IO[Model] = {
    StgModel.create(VisualStg(importStg(in), VisualModel.empty))
  }

  def importStg(in: InputStream): MathStg = {
    val helper: ParserHelper = new ParserHelper
    new DotGParser(in).parse(helper)
    val result: MathStg = helper.getStg
    return result;
  }
}
