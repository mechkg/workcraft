package org.workcraft.plugins.petrify

import scalaz.NonEmptyList
import org.workcraft.plugins.fsm.State
import org.workcraft.plugins.fsm.Arc
import org.workcraft.plugins.fsm.FSM

object FsmBuilder {
  import DotGParser._
  def buildFSM (dotg: DotG) = {
    val states = dotg.stateGraph.foldLeft(Set[String]())( (states, line) => (states + line._1.name) + line._3.name).toList.sorted
    
    val names = states.map (s => (s, new State)).toMap
    val labels = names.map(_.swap).toMap
    
    val stateList = names.map(_._2).toList
    
    val (arcs, arcLabels) = dotg.stateGraph.foldLeft( (List[Arc](), Map[Arc, String]()) ) { case ((arcs, labels), line) => {
      val newArc = new Arc(names(line._1.name), names(line._3.name))
      val label = line._2.toString
      
      ((newArc :: arcs), labels + (newArc -> label))
      }
    }
    
    val initialState = names(dotg.marking.head._1 match {
      case PlaceRef.ExplicitPlace(name) => name
      case _ => states.head
    })
    
    val finalStates = names.map(_._2).toSet
    
    FSM (NonEmptyList(stateList.head, stateList.tail:_*), arcs, finalStates, initialState, labels, arcLabels)
  }
}