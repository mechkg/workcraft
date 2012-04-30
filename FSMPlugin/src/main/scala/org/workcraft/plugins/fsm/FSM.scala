package org.workcraft.plugins.fsm
import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import scalaz.NonEmptyList

sealed trait Node

class State extends Node

class Arc(val from: State, val to: State) extends Node

case class FSM(states: NonEmptyList[State], arcs: List[Arc], finalStates: Set[State], initialState: State, labels: Map[State, String], arcLabels: Map[Arc, String]) {
  lazy val names = labels.map(_.swap).toMap

  lazy val postset: Map[State, List[(State, Arc)]] =
    arcs.foldLeft((Map[State, List[(State, Arc)]]().withDefault(_ => List()))) {
      case (map, arc) => (map + (arc.from -> ((arc.to, arc) :: map(arc.from))))
    }

  lazy val preset: Map[State, List[(State, Arc)]] =
    arcs.foldLeft((Map[State, List[(State, Arc)]]().withDefault(_ => List()))) {
      case (map, arc) => (map + (arc.to -> ((arc.from, arc) :: map(arc.to))))
    }
}

object FSM {
  def Minimal = {
    val st = new State
    FSM(NonEmptyList(st), List(), Set(), st, Map(st -> "s0"), Map())
  }
}

case class VisualFSM(fsm: FSM, layout: Map[State, Point2D.Double], visualArcs: Map[Arc, StaticVisualConnectionData])

object VisualFSM {
  def Minimal = {
    val fsm = FSM.Minimal
    VisualFSM(fsm, Map(fsm.states.head -> new Point2D.Double(0, 0)), Map())
  }
}
