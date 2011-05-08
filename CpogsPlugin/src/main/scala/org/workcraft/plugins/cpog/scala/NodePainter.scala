package org.workcraft.plugins.cpog.scala.nodes
import java.awt.Color
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.ColorisableGraphicalContent.Util._
import org.workcraft.plugins.cpog.scala.Graphics._
import org.workcraft.plugins.cpog.scala.VisualArc
import org.workcraft.plugins.cpog.scala.VisualVertex
import org.workcraft.plugins.cpog.scala.VisualVariable
import org.workcraft.plugins.cpog.scala.VisualRhoClause
import org.workcraft.plugins.cpog.scala.TouchableProvider
import org.workcraft.plugins.cpog.scala.Util.monadicSyntax
import java.awt.geom.AffineTransform
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions


object NodePainter {
  def nodeColorisableGraphicalContent (componentTransform: Component => Expression[AffineTransform])(node : Node) : Expression[ColorisableGraphicalContent] = node match {
    case v : Vertex    => for (t <- componentTransform(v); image <- VisualVertex.image (v)) yield transform (image, t).graphics
    case v : Variable  => for (t <- componentTransform(v); image <- VisualVariable.image (v)) yield transform (image, t).graphics
    case v : RhoClause => for (t <- componentTransform(v); image <- VisualRhoClause.image (v)) yield transform (image, t).graphics
    case a : Arc       => for (gui <- VisualArc.gui(TouchableProvider.touchable(componentTransform))(a)) yield gui.graphicalContent 
  }
  
  def nodeGraphicalContent (componentTransform : Component => Expression[AffineTransform])(colourisation : Node => Colorisation)(node : Node) : Expression[GraphicalContent] =
    for (gc <- nodeColorisableGraphicalContent (componentTransform)(node)) yield applyColourisation (gc, colourisation(node))
  
  private def composeExpr (a: Expression[GraphicalContent], b: Expression[GraphicalContent]) : Expression[GraphicalContent] =
    for (a <- a; b <- b) yield compose (a,b)
  
  def graphicalContent (componentTransform : Component => Expression[AffineTransform])(colourisation : Node => Colorisation)(nodes : List[Node]) : Expression[GraphicalContent] =
      nodes.map(nodeGraphicalContent(componentTransform)(colourisation)).foldLeft(Expressions.constant(GraphicalContent.EMPTY))(composeExpr)
}