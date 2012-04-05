package org.workcraft.plugins.petrify

import org.workcraft.plugins.petri2._
import PetriNet._

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

import scalaz.Scalaz_

object PetriNetBuilder {
  import DotGParser._

  private def directionStr (d: Direction): String = direction match {
    case Direction.Plus => "_plus"
    case Direction.Minus => "_minus"
    case Direction.Toggle => "_toggle"
  }

  private def instanceStr (i: Int): String = i match {
    case 0 => ""
    case _ => "_" + i.toString
  }

  private def getOrCreate (element: GraphElement, net: PetriNet): IO[(Component, PetriNet)] = element match {
       case SignalTransition (name, direction, instance) => {
        val pnname: String = name + directionStr(direction) + instanceStr(instance)
        if (net.names.contains(pnname))
          ioPure.pure { (net.names(pnname), net) }
         else newTransition.map(t => net.copy (transitions = t :: net.transitions, labelling = net.labelling + (t -> pnname)))
        }

        case PlaceOrDummy (name) => {
        if (net.names.contains(name))
          ioPure.pure { (net.names(name), net) }
        else
        if (dotg.dummy.contains(name))
          newTransition.map(t => net.copy (transitions = t :: net.transitions, labelling = net.labelling + (t -> name)))
        else
        newPlace.map(p => net.copy (places = p :: net.places, labelling = net.labelling + (p -> name)))
      }
  }


  def buildPetriNet (dotg: DotG): IO[PetriNet] = {
    dotg.graph.toList.foldLeft (ioPure.pure { PetriNet.Empty }) { case (net, (element, postset)) => {
      val createComponents: IO[(List[Component], PetriNet)] =
         (element :: postset).foldLeft ((net.map((List[Component](), _)))) { case (res, comp) => res >>= ( res => getOrCreate (comp, res._2).map (( _._1 :: res._1, _._2))) }

      val createArcs: IO[PetriNet] =
        createComponents >>= { case (components, net) => components.tail.foldLeft(net)((net, comp) => createArc(components.head, comp).map(arc => net.copy(arcs = net.arcs + arc)))}

/*      val applyMarking: IO[PetriNet] = 
        createArcs.map ( net => dotg.marking*/
      }
  }
  }
}
