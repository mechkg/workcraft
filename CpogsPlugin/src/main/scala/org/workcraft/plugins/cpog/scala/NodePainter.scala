package org.workcraft.plugins.cpog.scala
import java.awt.geom.AffineTransform
import org.workcraft.dom.visual.ColorisableGraphicalContent.Util.applyColourisation
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.graphics.Graphics.asBCGC
import org.workcraft.graphics.Graphics.compose
import org.workcraft.graphics.Graphics.transform
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz.maImplicit
import nodes.Arc
import nodes.Component
import nodes.Node
import nodes.RhoClause
import nodes.Variable
import nodes.Vertex
import org.workcraft.graphics.RichGraphicalContent


object NodePainter {
  
  def componentColorisableGraphicalContentLocal (component : Component) : Expression[RichGraphicalContent] = component match {
    case v : Vertex    => VisualVertex.image (v)
    case v : Variable  => VisualVariable.image (v)
    case v : RhoClause => VisualRhoClause.image (v)
  }
  
  def nodeColorisableGraphicalContent (componentTransform: Component => Expression[AffineTransform])(node : Node) : Expression[ColorisableGraphicalContent] = node match {
    case c : Component => for (t <- componentTransform(c); image <- componentColorisableGraphicalContentLocal(c)) yield transform (image, t).graphics
    case a : Arc =>
      for (first  <- TouchableProvider.touchable(componentTransform)(a.first);
           second <- TouchableProvider.touchable(componentTransform)(a.second);
           visual <- (a.visual : Expression[VisualArc]);
           gui <- VisualArc.gui(first, second, visual)) yield gui.graphicalContent 
  }
  
  def nodeGraphicalContent (componentTransform : Component => Expression[AffineTransform])(colourisation : Node => Colorisation)(node : Node) : Expression[GraphicalContent] =
    for (gc <- nodeColorisableGraphicalContent (componentTransform)(node)) yield applyColourisation (gc, colourisation(node))
  
  private def composeExpr (a: Expression[GraphicalContent], b: Expression[GraphicalContent]) : Expression[GraphicalContent] =
    for (a <- a; b <- b) yield compose (a,b)
  
  
  def graphicalContent (componentTransform : Component => Expression[AffineTransform])(colourisation : Node => Colorisation)(nodes : List[Node]) : Expression[GraphicalContent] =
      nodes.map(nodeGraphicalContent(componentTransform)(colourisation)).foldLeft(constant(GraphicalContent.EMPTY))(composeExpr)
}
