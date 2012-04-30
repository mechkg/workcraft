package org.workcraft.plugins.petri2

import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline

object EditablePetriNet {
  def create(initialState: VisualPetriNet): IO[EditablePetriNet] = for {
    marking <- newVar(initialState.net.marking);
    labelling <- newVar(initialState.net.labelling);
    names <- newVar(initialState.net.names);
    places <- newVar(initialState.net.places);
    transitions <- newVar(initialState.net.transitions);
    arcs <- newVar(initialState.net.arcs);
    layout <- newVar(initialState.layout);
    visualArcs <- newVar(initialState.visualArcs)
  } yield new EditablePetriNet(marking, labelling, names, places, transitions, arcs, layout, visualArcs)
}

class EditablePetriNet private (
  val marking: Variable[Map[Place, Int]],
  val labelling: Variable[Map[Component, String]],
  val names: Variable[Map[String, Component]],
  val places: Variable[List[Place]],
  val transitions: Variable[List[Transition]],
  val arcs: Variable[List[Arc]],
  val layout: Variable[Map[Component, Point2D.Double]],
  val visualArcs: Variable[Map[Arc, StaticVisualConnectionData]]) {

  import PetriNet._

  val nodes: Expression[List[Node]] = (places.expr <***> (transitions, arcs))((p, t, a) => a ++ t ++ p)

  val components: Expression[List[Component]] = (places.expr <**> transitions)((p, t) => p ++ t)

  val incidentArcs: Expression[Map[Component, List[Arc]]] = (arcs.expr <**> components)((arcs, components) => components.map(c => (c, arcs.filter(arc => (arc.to == c) || (arc.from == c)))).toMap)

  val transitionIncidence: Expression[(Map[Transition, List[Place]], Map[Transition, List[Place]])] =
    arcs.map (_.foldLeft((Map[Transition, List[Place]]().withDefault(_ => List()), Map[Transition, List[Place]]().withDefault(_ => List())))({
      case ((prod, cons), arc) => arc match {
        case c: ConsumerArc => (prod, cons + (c.to -> (c.from :: cons(c.to))))
          case p: ProducerArc => (prod + (p.from -> (p.to :: prod(p.from))), cons)
	}
    }))

  def tokens(place: Place) = marking.map(_(place))
  def label(c: Component) = labelling.map(_(c))

  private var placeNameCounter = 0
  private var transitionNameCounter = 0

  private def newPlaceName =
    names.eval >>= (names => ioPure.pure {
      def name = "p" + placeNameCounter
      while (names.contains(name)) placeNameCounter += 1
      val result = name
      placeNameCounter += 1
      result
    })

  private def newTransitionName =
    names.eval >>= (names => ioPure.pure {
      def name = "t" + transitionNameCounter
      while (names.contains(name)) transitionNameCounter += 1
      val result = name
      transitionNameCounter += 1
      result
    })

  def createPlace(where: Point2D.Double): IO[Place] = for {
    p <- newPlace;
    name <- newPlaceName;
    _ <- places.update(p :: _);
    _ <- marking.update(_ + (p -> 0));
    _ <- labelling.update(_ + (p -> name))
    _ <- names.update(_ + (name -> p))
    _ <- layout.update(_ + (p -> where))
  } yield p

  def createTransition(where: Point2D.Double): IO[Transition] = for {
    t <- newTransition;
    name <- newTransitionName;
    _ <- transitions.update(t :: _);
    _ <- labelling.update(_ + (t -> name))
    _ <- names.update(_ + (name -> t))
    _ <- layout.update(_ + (t -> where))
  } yield t

  def createProducerArc(from: Transition, to: Place) = for {
    arc <- newProducerArc(from, to);
     _ <- arcs.update(arc :: _);
     _ <- visualArcs.update( _ + (arc -> Polyline(List())))
  } yield arc

  def createConsumerArc(from: Place, to: Transition) = for {
    arc <- newConsumerArc(from, to);
     _ <- arcs.update(arc :: _);
     _ <- visualArcs.update( _ + (arc -> Polyline(List())))
  } yield arc

  def deleteArc(a: Arc) = arcs.update(_ - a) >>=| visualArcs.update(_ - a)

  private def deleteComponent(c: Component) = (incidentArcs.eval <|*|> labelling.eval) >>= { case (a, l) => a(c).map(deleteArc(_)).sequence >>=| names.update(_ - l(c)) >>=| labelling.update(_ - c) >>=| layout.update (_ - c)}

  def deletePlace(p: Place) = deleteComponent(p) >>=| places.update(_ - p)
  
  def deleteTransition(t: Transition) = deleteComponent(t) >>=| transitions.update(_ - t)
  
  def deleteNode(n: Node): IO[Unit] = n match {
    case p: Place => deletePlace(p)
    case t: Transition => deleteTransition(t)
    case a: Arc => deleteArc(a)
  }

  def deleteNodes(nodes: Set[Node]): IO[Unit] = nodes.toList.map(deleteNode(_)).sequence >| {}

  def saveState = for {
    places <- places;
    transitions <- transitions;
    arcs <- arcs;
    labelling <- labelling;
    marking <- marking;
    layout <- layout;
    visualArcs <- visualArcs
  } yield VisualPetriNet(PetriNet(marking, labelling, places, transitions, arcs), layout, visualArcs)

  def loadState(state: VisualPetriNet): IO[Unit] =
    marking.set(state.net.marking) >>=| labelling.set(state.net.labelling) >>=| names.set(state.net.names) >>=|
      places.set(state.net.places) >>=| transitions.set(state.net.transitions) >>=| arcs.set(state.net.arcs) >>=|
      layout.set(state.layout) >>=| visualArcs.set(state.visualArcs)
}
