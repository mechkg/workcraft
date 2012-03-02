/* package org.workcraft.graphics
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

trait Node

trait Graphicz {
  def deriveColorisable: Colorisable[Graphicz]
  def transform(t: AffineTransform): Graphicz
}

trait Colorisable[T] {
  def applyColorisation[T](colorisation: Colorisation): T
}

trait Bounded {
  def bounds: BoundingBox
}

trait Transformable[A] {
  def transform[AffineTransform](t: A) : A
}

trait Monoid[A] {
  def empty: A
  def *(a: A, b: A):A  
}

object Ops {
	def align[A] (what: Bounded with Transformable[A], to: Bounded): A = null
	def compose[A] (list: List[A])(implicit m: Monoid[A]) = null
}

object Painter {
  def cull (visibleArea: Rectangle2D.Double, bb: Option[BoundingBox]): Boolean = bb match {
    case Some (bb) /* if bb outside of visibleArea */ => false 
    case _ => true
  }
  
  def paint (n: List[Node],
             visibleArea: Rectangle2D.Double,
             g: Node => Graphicz,
             t: Node => AffineTransform, // or just tranlsation, but rotation is also useful for asymmetrical objects
             b: Node => Option[BoundingBox])(implicit m: Monoid[Graphicz]): Graphicz = {
    
    val pvs = n.filter( node => cull(visibleArea, b(node).map(_.transform(t(node)))))
    
    val transformedPvs = pvs.map(node => g(node).transform(t(node)))
    
    transformedPvs.foldLeft (m.empty)(m.*(_,_))
  }
}*/