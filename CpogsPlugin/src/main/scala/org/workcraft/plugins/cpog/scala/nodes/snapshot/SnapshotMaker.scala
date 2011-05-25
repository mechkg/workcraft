package org.workcraft.plugins.cpog.scala.nodes.snapshot

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.scala.{nodes => M}
import org.workcraft.plugins.cpog.scala.Expressions._
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Scalaz._
import java.util.UUID

import scala.collection.immutable.Map

object SnapshotMaker {
  var snapshots = Map.empty[M.Node, Node];
  
  case class ExprCpogBuilder[+T](expr : Expression[_ <: CpogBuilder[T]]) {
    def map[R](f : T => R) : ExprCpogBuilder[R] = asECB(for(b <- expr) yield for(t <- b) yield f(t))
    def ap[R](f : ExprCpogBuilder[T => R]) : ExprCpogBuilder[R] = asECB(for(a <- expr; f <- f.expr) yield for(a <- a; f <- f) yield f(a))
  }
  object ExprCpogBuilder {
    def lift[A,B,R](f : (A, B) => R) = (x : ExprCpogBuilder[A], y : ExprCpogBuilder[B]) => y.ap(for(x <- x) yield f(x,_:B))
    def map2[A,B,R](x : ExprCpogBuilder[A], y : ExprCpogBuilder[B], f : (A, B) => R) = lift(f)(x,y)
  }
  import ExprCpogBuilder._
  
  def asECB[T](expr : Expression[CpogBuilder[T]]) : ExprCpogBuilder[T] = new ExprCpogBuilder(expr)
  
  import CpogBuilder.emptyBuilder
  
  def mapEC[T](l : List[ExprCpogBuilder[T]]) : ExprCpogBuilder[List[T]] = {
    val default = asECB(constant(emptyBuilder(Nil : List[T])))
    l.foldRight (default) (lift(_::(_:List[T])))
  }
  
  def mapC[T](l : List[CpogBuilder[T]]) : CpogBuilder[List[T]] = {
    l.foldRight(emptyBuilder(Nil:List[T]))((h, t) => for(h <- h; t <- t) yield h :: t)
  }
  
  def makeSnapshot(nodes: List[M.Node]): CpogBuilder[Any] = {
    mapC(nodes.map(makeSnapshot(_: M.Node)))
  }
  
  def newId[T] : Id[T] = {
    new Id[T](UUID.randomUUID)
  }
  
  def snapshotVariableData(v : M.Variable) : Expression[Variable] = {
    v match {
      case M.Variable(state, label, visualProperties) => for(state <- state ; label <- label; visualProperties <- makeSnapshot(visualProperties)) yield Variable(state, label, visualProperties)
    }
  }
  
  def snapshotVariable(v : M.Variable) : CpogBuilder[Id[Variable]] = new CpogBuilder[Id[Variable]] {
    def buildWithCpog(builder : CpogBuilding) : (CpogBuilding, Id[Variable]) = {
      var resB = builder;
      val id = builder.varCache.getOrElse(v, {
        val nId = newId[Variable]
        val cpog2 = for(cpog <- builder.cpog; varData <- snapshotVariableData(v))
          yield cpog match {
          case CPOG(variables, vertices, arcs, rhoClauses) => new CPOG(variables + ((nId, varData)), vertices, arcs, rhoClauses)
          }
        resB = new CpogBuilding(cpog2, builder.varCache+((v, nId)), builder.vertexCache)
        nId
      })
      (resB, id)
    }
  }

  def makeSnapshot(node: M.Node): CpogBuilder[Node] = {
    node match {
      case c: M.Component => makeSnapshot(c)
      case a: M.Arc => makeSnapshot(a)
    }
  }

  def makeSnapshot(component: M.Component): ExprCpogBuilder[Component] = {
    asECB(component match {
      case M.Vertex(condition, visualProperties) =>
        for (
          condition <- condition;
          visualProperties <- makeSnapshot(visualProperties)
        ) yield {
          for(c <- makeSnapshot(condition))
        	  yield new Vertex(c, visualProperties) 
        }
    })
  }

  def makeSnapshot(component: M.VisualProperties): Expression[VisualProperties] = {
    for (
      label <- component.label;
      position <- component.position;
      labelPositioning <- component.labelPositioning
    ) yield new VisualProperties(label, labelPositioning, position)
  }

  def makeSnapshot(condition: BooleanFormula[M.Variable]): CpogBuilder[BooleanFormula[Id[Variable]]] = {
    JoinBooleanFormula.joinBooleanFormula(VariableReplacer.replace(asFunctionObject((v: M.Variable) => snapshotVariable(v)), condition))
  }
}
