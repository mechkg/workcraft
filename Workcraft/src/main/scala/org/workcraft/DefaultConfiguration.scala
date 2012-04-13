package org.workcraft

import org.workcraft.plugins.petri2.PetriNetModule
import org.workcraft.plugins.petrify.PetrifyModule
import org.workcraft.plugins.lola.LolaModule
import org.workcraft.plugins.fsm.FSMModule
import org.workcraft.plugins.dot.DotModule
import org.workcraft.services.GlobalServiceManager
import java.io.File

object DefaultConfiguration {

  val services = new GlobalServiceManager(
    List(new PetriNetModule, new PetrifyModule,
	 new FSMModule, new LolaModule, new DotModule ))


  def main (args: Array[String]) = {
    org.workcraft.gui.Main.main ("default configuration", services, args)
  }
}
