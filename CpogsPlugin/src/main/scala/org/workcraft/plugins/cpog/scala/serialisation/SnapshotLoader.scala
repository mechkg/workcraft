package org.workcraft.plugins.cpog.scala.serialisation

import org.workcraft.plugins.cpog.scala.{nodes => M}
import org.workcraft.plugins.cpog.scala.{VisualArc => MVisualArc}
import org.workcraft.plugins.cpog.scala.nodes.{snapshot => P}
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import java.awt.geom.Point2D
import pcollections.TreePVector
import pcollections.PVector

object SnapshotLoader {

  def makePVector[A](iter : Iterable[A]) = ((TreePVector.empty[A] : PVector[A]) /: iter)((v : PVector[A], a : A) => v.plus(a))
  
  def load(cpog : P.CPOG, sm : StorageManager) : org.workcraft.plugins.cpog.CPOG = {
    def loadVisualProperties(prop : P.VisualProperties) = {
      val P.VisualProperties(label, labelPositioning, position) = prop
      M.VisualProperties(sm.create(label), sm.create(labelPositioning), sm.create(position))
    }
    
    val P.CPOG (variables, vertices, arcs, rhoClauses) = cpog
    val mVariables = variables.map({ case (k, P.Variable(state, visual)) => (k, new M.Variable(sm.create(state), loadVisualProperties(visual))) }).toMap
    def formulaReplacer(formula : BooleanFormula[P.Id[P.Variable]]) = VariableReplacer.replace(asFunctionObject(mVariables.apply), formula)
    
    // can't use mapValues here because it creates a view!
    val mVertices = vertices.map({ case (k, P.Vertex(condition, visual)) => (k, {println("creating vertex"); new M.Vertex(sm.create(formulaReplacer(condition)), loadVisualProperties(visual))})})
    
    def loadVisualArc(arc : P.VisualArc) = arc match {
      case P.VisualArc.Bezier(cp1, cp2) => MVisualArc.Bezier(sm.create(cp1), sm.create(cp2))
      case P.VisualArc.Polyline(cps) => MVisualArc.Polyline(for(cp <- cps) yield sm.create(cp))
    }
    val mArcs = arcs.map({case P.Arc(first, second, condition, visual) => new M.Arc(mVertices(first), mVertices(second), sm.create(formulaReplacer(condition)), sm.create(loadVisualArc(visual)))})
    
    val mRhoClauses = for(P.RhoClause(formula, visual) <- rhoClauses) yield M.RhoClause(sm.create(formulaReplacer(formula)), loadVisualProperties(visual))
    
    new org.workcraft.plugins.cpog.CPOG(sm, makePVector(mVariables.values), makePVector(mVertices.values), makePVector(mRhoClauses), makePVector(mArcs))
  }
}
