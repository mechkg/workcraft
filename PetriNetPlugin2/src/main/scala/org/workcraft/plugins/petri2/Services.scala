package org.workcraft.plugins.petri2

import org.workcraft.services.ModelService
import org.workcraft.scala.effects.IO

object PetriNetService extends ModelService[IO[PetriNet]]
object VisualPetriNetService extends ModelService[IO[VisualPetriNet]]
