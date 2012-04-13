package org.workcraft.plugins.petrify

import org.workcraft.plugins.petri2._
import PetriNet._

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

import scalaz.Scalaz._

object PetriNetBuilder {
  import DotGParser._
  import GraphElement._

  private def directionStr(direction: Direction): String = direction match {
    case Direction.Plus => "_plus"
    case Direction.Minus => "_minus"
    case Direction.Toggle => "_toggle"
  }

  private def instanceStr(i: Int): String = i match {
    case 0 => ""
    case _ => "_" + i.toString
  }

  private def signalTransitionName(st: SignalTransition) = st.name + directionStr(st.direction) + instanceStr(st.instance)

  private def getOrCreate(dummy: List[String], element: GraphElement, net: PetriNet): IO[(Component, PetriNet)] = element match {
    case x @ SignalTransition(name, direction, instance) => {
      val pnname = signalTransitionName(x)
      if (net.names.contains(pnname))
        ioPure.pure { (net.names(pnname), net) }
      else newTransition.map(t => (t, net.copy(transitions = t :: net.transitions, labelling = net.labelling + (t -> pnname))))
    }

    case PlaceOrDummy(name) => {
      if (net.names.contains(name))
        ioPure.pure { (net.names(name), net) }
      else if (dummy.contains(name))
        newTransition.map(t => (t, net.copy(transitions = t :: net.transitions, labelling = net.labelling + (t -> name))))
      else
        newPlace.map(p => (p, net.copy(places = p :: net.places, labelling = net.labelling + (p -> name))))
    }
  }

  // Builds a Petri Net from a .g file AST
  def buildPetriNet(dotg: DotG): IO[PetriNet] = {
    val z = ioPure.pure { (Map[(Transition, Transition), Place](), PetriNet.Empty) }

    val buildNet = dotg.graph.toList.foldLeft(z) {
      case (state, (element, postset)) => state >>= { case (implicitPlaces, net) => {
        // For each line in the STG graph, first create the components (transitions and places) referred to in this line:
        // -- if a reference is a SignalTransition, build a Petri Net compatible name (e.g. a+/2 -> a_plus_2) and add a transition
        //    (or return existing one if it is already known)
        // -- if a reference is a PlaceOrDummy, check if the name is in the list of dummies and make a transition if it is in the
        //    list or a place otherwise (or return existing place/transition if this name is already known)
        // TODO: maybe add support for instanced dummies?
        // Gather the list of components coresponding to the references in the STG and the state of the Petri Net so far.

        val createComponents: IO[(List[Component], PetriNet)] = {
          val z = ioPure.pure { (List[Component](), net) }
          (element :: postset).foldLeft(z) {
            case (result, element) => result >>= {
              case (comps, net) => getOrCreate(dotg.dummy, element, net).map { case (newComp, newNet) => (newComp :: comps, newNet) }
            }
          }
        }.map { case (components, net) => (components.reverse, net) }

        // create arcs from the first component in the list to the rest of the components and add them to the net
        // if both components are transitions, an implicit place needs to be created explicitly in the Petri Net
        val createArcs: IO[(Map[(Transition, Transition), Place], PetriNet)] = createComponents >>= { case (components, net) => {
            def ensureUniqueName(name: String, net: PetriNet): String = {
              def findFreeName(suffix: Int): String = {
                val candidate = name + "_" + suffix
                if (net.names.contains(candidate)) findFreeName(suffix + 1) else candidate
              }
              if (net.names.contains(name)) findFreeName(0) else name
            }

            val z = ioPure.pure { (implicitPlaces, net) }

            components.tail.foldLeft(z) {
              case (result, comp) => (components.head, comp) match {
                case (p: Place, t: Transition) => result >>= { case (implicitPlaces, net) => newConsumerArc(p, t).map(arc => (implicitPlaces, net.copy(arcs = arc :: net.arcs))) }
                case (t: Transition, p: Place) => result >>= { case (implicitPlaces, net) => newProducerArc(t, p).map(arc => (implicitPlaces, net.copy(arcs = arc :: net.arcs))) }
                case (t1: Transition, t2: Transition) => {
                  val name = ensureUniqueName(net.labelling(t1) + "_" + net.labelling(t2), net)
                  for {
                    result <- result;
                    p <- newPlace;
                    arc1 <- newProducerArc(t1, p);
                    arc2 <- newConsumerArc(p, t2)
                  } yield {
                    val (implicitPlaces, net) = result
                    ((implicitPlaces + ((t1, t2) -> p)), net.copy(arcs = arc1 :: arc2 :: net.arcs, places = p :: net.places,
								  labelling = net.labelling + (p -> name)))
                  }
                }
                case _ => throw new RuntimeException("The STG specification contains an invalid arc between places")
              }
            }
        }
      }
      
      createArcs
    }
    }
  }

    val applyMarking: IO[PetriNet] = buildNet map {
      case (implicitPlaces, net) => {
        def elementName(g: GraphElement) = g match {
          case x: SignalTransition => signalTransitionName(x)
          case PlaceOrDummy(name) => name
        }
        def findPlace(net: PetriNet, name: String): Place = net.names(name) match {
          case p: Place => p
          case _ => throw new RuntimeException(name + " is used as a place but is a transition")
        }
        def findTransition(net: PetriNet, name: String): Transition = net.names(name) match {
          case t: Transition => t
          case _ => throw new RuntimeException(name + " is used as a transition but is a place")
        }

        dotg.marking.foldLeft(net) {
          case (net, (ref, marking)) => ref match {
            case PlaceRef.ExplicitPlace(name) => net.copy(marking = net.marking + (findPlace(net, name) -> marking))

            case PlaceRef.ImplicitPlace(el1, el2) => {
              val t1 = findTransition(net, elementName(el1))
              val t2 = findTransition(net, elementName(el2))
              val p = implicitPlaces((t1, t2))
              net.copy(marking = net.marking + (p -> marking))
            }
          }
        }
      }
    }

    applyMarking.map ( net => {
      val markingWithDefaultZeroes = net.places.foldLeft (net.marking) ( (marking, place) => if (!marking.contains(place)) marking + (place -> 0) else marking)
      net.copy (marking = markingWithDefaultZeroes)
    })
  }
}
