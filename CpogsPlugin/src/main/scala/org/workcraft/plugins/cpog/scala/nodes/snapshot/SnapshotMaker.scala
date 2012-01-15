package org.workcraft.plugins.cpog.scala.nodes.snapshot

import org.workcraft.exceptions.NotImplementedException
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.scala.{nodes => M}
import org.workcraft.plugins.cpog.scala.{VisualArc => MVisualArc}
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import java.util.UUID
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.scala.BooleanFormulaInstances._

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
  
  def asECB[T](expr : Expression[_ <: CpogBuilder[T]]) : ExprCpogBuilder[T] = new ExprCpogBuilder(expr)
  
  import CpogBuilder.emptyBuilder
  
  def mapEC[T](l : List[ExprCpogBuilder[T]]) : ExprCpogBuilder[List[T]] = {
    val default = asECB(constant(emptyBuilder(Nil : List[T])))
    l.foldRight (default) (lift(_::(_:List[T])))
  }
  
  def mapEC_(l : List[ExprCpogBuilder[Unit]]) : ExprCpogBuilder[Unit] = {
    val default = asECB(constant(emptyBuilder(())))
    l.foldRight (default) (lift((_, _) => ()))
  }
  
  def mapC[T](l : List[CpogBuilder[T]]) : CpogBuilder[List[T]] = {
    l.foldRight(emptyBuilder(Nil:List[T]))((h, t) => for(h <- h; t <- t) yield h :: t)
  }
  
  def makeSnapshot(nodes: Expression[List[M.Node]]) : CPOG = {
    eval(
    (for(
      nodes <- nodes;
      cpog <- doMakeSnapshot(nodes)
      ) yield {cpog}))
  }
  
  def doMakeSnapshot(nodes : List[M.Node]) : Expression[CPOG] = {
    makeSnapshot(nodes).flatMap(builder => {
      val (CpogBuilding(cpog, _, _), _) = builder.buildWithCpog(CpogBuilding(constant(CPOG(Map.empty, Map.empty, List.empty, List.empty)), Map.empty, Map.empty))
	  cpog
    })
  }
  
  def makeSnapshot(nodes: List[M.Node]): Expression[_ <: CpogBuilder[Unit]] = {
    val nodeSnapshoter : M.Node => ExprCpogBuilder[Unit] = (n: M.Node) => asECB[Unit](makeSnapshot(n))
    mapEC_(nodes.map(nodeSnapshoter)).expr
  }
  
  def newId[T] : Id[T] = {
    new Id[T](UUID.randomUUID)
  }
  
  def snapshotVariableData(v : M.Variable) : Expression[Variable] = {
    v match {
      case M.Variable(state, visualProperties) => for(state <- state ; visualProperties <- makeSnapshot(visualProperties)) yield Variable(state, visualProperties)
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
  
  def addArc(arc : Arc) : CpogBuilder[Unit] = new CpogBuilder[Unit] {
    def buildWithCpog(builder : CpogBuilding) : (CpogBuilding, Unit) = {
      val CpogBuilding(cpog, varCache, vertexCache) = builder
      val cpog2 = for(cpog <- cpog) yield {
        val CPOG(variables, vertices, arcs, rhoClauses) = cpog
        CPOG(variables, vertices, arc :: arcs, rhoClauses)
      }
      (CpogBuilding(cpog2, varCache, vertexCache),())
    }
  }

  def makeSnapshot(node: M.Node): Expression[_ <: CpogBuilder[Unit]] = {
    node match {
      case c: M.Component => makeComponentSnapshot(c)
      case a: M.Arc => makeArcSnapshot(a)
    }
  }
  
  def makeArcSnapshot(arc : M.Arc) : Expression[CpogBuilder[Unit]] = {
    val M.Arc(first, second, condition, visual) = arc
    for(first <- snapshotVertex(first);
    	second <- snapshotVertex(second);
    	condition <- condition;
    	visual <- visual;
    	visual <- makeVisualArcSnapshot(visual)
        ) yield for(first <- first; second <- second; condition <- makeSnapshot(condition); unit <- addArc(Arc(first, second, condition, visual)))
        	yield unit
  }
  
  def makeVisualArcSnapshot(visualArc : MVisualArc) : Expression[VisualArc] = {
    visualArc match {
      case MVisualArc.Bezier(cp1, cp2) => for(cp1 <- cp1; cp2 <- cp2) yield VisualArc.Bezier(cp1, cp2)
      case MVisualArc.Polyline(cps) => for(cps <- (for( me <- cps) yield me.expr).sequence[Expression, Point2D.Double](
    		  implicitly[<:<[Expression[Point2D.Double],Expression[Point2D.Double]]], 
    		  implicitly[scalaz.Traverse[List]], 
    		  implicitly[scalaz.Applicative[Expression]])
          ) yield VisualArc.Polyline(cps)
    }
  }

  def snapshotRhoClause(rho : M.RhoClause) : Expression[CpogBuilder[Unit]] = {
    for(data <- snapshotRhoClauseData(rho)) yield 
    data.flatMap(data =>
    new CpogBuilder[Unit]{
      def buildWithCpog(cpogBuilding : CpogBuilding) = {
        val CpogBuilding(cpog, varCache, vertexCache) = cpogBuilding
        val cpog2 = for(cpog <- cpog) yield {
          val CPOG(variables, vertices, arcs, rhoClauses) = cpog
          CPOG(variables, vertices, arcs, data :: rhoClauses)
        }
        (CpogBuilding(cpog2, varCache, vertexCache), ())
      }
    })
  }
  
  def snapshotRhoClauseData(rho : M.RhoClause) : Expression[CpogBuilder[RhoClause]] = {
    val M.RhoClause(formula, visual) = rho
    for(formula <- formula; visual <- makeSnapshot(visual)) yield {
      for(formula <- makeSnapshot(formula)) yield {
        RhoClause(formula, visual)
      }
    }
  }
  
  
  def snapshotVertex(vertex : M.Vertex) : Expression[CpogBuilder[Id[Vertex]]] = {
    val M.Vertex(condition, visualProperties) = vertex
    for (
      condition <- condition
    ) yield new CpogBuilder[Id[Vertex]] {
      def buildWithCpog(builder : CpogBuilding) : (CpogBuilding, Id[Vertex]) = {
        val CpogBuilding(_, _, vertexCache) = builder
        val res : Option[(CpogBuilding, Id[Vertex])] = for(id <- vertexCache.get(vertex)) yield (builder, id)
        res.getOrElse({
          val id = newId[Vertex]
          
          val (CpogBuilding(cpog, varCache, vertexCache), condition2) = makeSnapshot(condition).buildWithCpog(builder)
          
          val cpog2 = for(cpog <- cpog; visualProperties <- makeSnapshot(visualProperties)) yield {
            val CPOG(variables, vertices, arcs, rhoClauses) = cpog
            new CPOG(variables, vertices + ((id, Vertex(condition2, visualProperties))), arcs, rhoClauses)
          }
          System.out.println(vertexCache.size.toString)
          (new CpogBuilding(cpog2, varCache, vertexCache+((vertex, id))), id)
        })
      }
    }
  }
  
  def ignore(e : Expression[_ <: CpogBuilder[_]]) : Expression[CpogBuilder[Unit]] = for (cb <- e) yield for (_ <- cb) yield ()
  
  def makeComponentSnapshot(component: M.Component): Expression[CpogBuilder[Unit]] = {
    component match {
      case v : M.Vertex => ignore(snapshotVertex(v))
      case v : M.Variable => ignore(constant(snapshotVariable(v)))
      case r : M.RhoClause => ignore(snapshotRhoClause(r))
    }
  }

  def makeSnapshot(component: M.VisualProperties): Expression[VisualProperties] = {
    for (
      label <- component.label;
      position <- component.position;
      labelPositioning <- component.labelPositioning
    ) yield new VisualProperties(label, labelPositioning, position)
  }

  def makeSnapshot(condition: BooleanFormula[M.Variable]): CpogBuilder[BooleanFormula[Id[Variable]]] = {
    (for(v <- condition) yield snapshotVariable(v)).sequence
  }
}
