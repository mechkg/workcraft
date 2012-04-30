package org.workcraft.plugins.fsm

import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.plugins.petri2.NameGenerator
import scalaz.NonEmptyList

class EditableFSM(
  val states: ModifiableExpression[NonEmptyList[State]],
  val arcs: ModifiableExpression[List[Arc]],
  val labels: ModifiableExpression[Map[State, String]],
  val stateNames: ModifiableExpression[Map[String, State]],
  val arcLabels: ModifiableExpression[Map[Arc, String]],
  val finalStates: ModifiableExpression[Set[State]],
  val initialState: ModifiableExpression[State],
  val layout: ModifiableExpression[Map[State, Point2D.Double]],
  val visualArcs: ModifiableExpression[Map[Arc, StaticVisualConnectionData]]) {

  val nodes = (states.expr <**> arcs)(_.list ++ _)

  val nameGen = NameGenerator(stateNames, "s")

  val incidentArcs: Expression[Map[State, List[Arc]]] = (arcs.expr <**> states)((arcs, states) => states.list.map(c => (c, arcs.filter(arc => (arc.to == c) || (arc.from == c)))).toMap)

  val presetV = saveState.map (_.fsm.preset)
  val postsetV = saveState.map (_.fsm.postset)

  def preset(s: State) = presetV.map(_(s))
  def postset(s: State) = postsetV.map(_(s))

  private def newState = ioPure.pure { new State }

  private def newArc(from: State, to: State) = ioPure.pure { new Arc(from, to) }

  def createState(where: Point2D.Double): IO[State] = for {
    s <- newState;
    name <- nameGen.newName;
    _ <- states.update( s <:: _);
    _ <- labels.update(_ + (s -> name));
    _ <- stateNames.update(_ + (name -> s));
    _ <- layout.update(_ + (s -> where))
  } yield s

  def createArc(from: State, to: State) = for {
    arc <- newArc(from, to);
    _ <- arcs.update(arc :: _);
    _ <- arcLabels.update(_ + (arc -> ""))
    _ <- visualArcs.update(_ + (arc -> Polyline(List())))
  } yield arc

  def deleteNode(n: Node): IO[Unit] = n match {
    case s: State => deleteState(s)
    case a: Arc => deleteArc(a)
  }

  def deleteArc(a: Arc) = arcs.update(_ - a) >>=| arcLabels.update (_ - a) >>=| visualArcs.update(_ - a)

  def remove[A](list: NonEmptyList[A], what: A): NonEmptyList[A] = {
    if (list.head == what)
      (if (list.tail != Nil)
        NonEmptyList(list.tail.head, list.tail.tail:_*)
      else throw new RuntimeException("cannot remove last element from NonEmptyList"))
    else
      NonEmptyList(list.head, (list.tail - what):_*)
  }

  def deleteState(s: State) =
    (incidentArcs.eval <|***|> (labels.eval, initialState.eval, states.eval)) >>= {
      case (a, l, ist, st) =>
        val del = a(s).map(deleteArc(_)).sequence >>=| states.update(remove(_, s)) >>=| layout.update(_ - s) >>=| stateNames.update(_ - l(s)) >>=| labels.update(_ - s) >>=| finalStates.update(_ - s)

        val updateInitial =  if (ist == s) states.eval >>= (st => initialState.set(st.head)) else IO.Empty

        del >>=| updateInitial
    }

  def deleteNodes(nodes: Set[Node]): IO[Either[String, IO[Unit]]] = states.eval.map(states => {
    if ((nodes.count { case s: State => true; case _ => false }) == states.tail.length + 1)
      Left("Cannot delete selection: at least one state must be defined.")
    else
      Right(nodes.toList.map(deleteNode(_)).sequence >| {})
  })

  def saveState = for {
    states <- states;
    arcs <- arcs;
    finalStates <- finalStates;
    initial <- initialState;
    labels <- labels;
    arcLabels <- arcLabels;
    layout <- layout;
    visualArcs <- visualArcs
  } yield VisualFSM(FSM(states, arcs, finalStates, initial, labels, arcLabels), layout, visualArcs)

  def loadState(state: VisualFSM): IO[Unit] =
    states.set(state.fsm.states) >>=| labels.set(state.fsm.labels) >>=| stateNames.set(state.fsm.names) >>=| finalStates.set(state.fsm.finalStates) >>=|
      initialState.set(state.fsm.initialState) >>=| arcs.set(state.fsm.arcs) >>=| arcLabels.set(state.fsm.arcLabels) >>=| layout.set(state.layout) >>=| visualArcs.set(state.visualArcs)
}

object EditableFSM {
  def create(initialState: VisualFSM) = for {
    states <- newVar(initialState.fsm.states);
    arcs <- newVar(initialState.fsm.arcs);
    labels <- newVar(initialState.fsm.labels);
    stateNames <- newVar(initialState.fsm.names);
    arcLabels <- newVar(initialState.fsm.arcLabels);
    finalStates <- newVar(initialState.fsm.finalStates);
    initial <- newVar(initialState.fsm.initialState);
    layout <- newVar(initialState.layout);
    visualArcs <- newVar(initialState.visualArcs)
  } yield new EditableFSM(states, arcs, labels, stateNames, arcLabels, finalStates, initial, layout, visualArcs)
}
