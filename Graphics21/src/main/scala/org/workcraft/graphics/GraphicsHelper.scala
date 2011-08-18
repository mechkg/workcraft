package org.workcraft.graphics
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.gui.graph.tools.Colorisation

object GraphicsHelper {
  def paintNodes[N](painter: N => Expression[GraphicalContent], nodes: Expression[_ <: Iterable[N]]) =
    for (
      nodes <- nodes;
      graphics <- nodes.map(painter).toList.sequence
    ) yield graphics.foldLeft(GraphicalContent.EMPTY)(Graphics.compose)

  def paint[N](painter: N => Expression[ColorisableGraphicalContent], nodes: Expression[_ <: Iterable[N]]) = paintNodes(dontColourise(painter), nodes)

  def paintWithHighlights[N](
    painter: N => Expression[ColorisableGraphicalContent],
    nodes: Expression[_ <: Iterable[N]]) =
    (highlightedColorisation: Colorisation,
      highlighted: Expression[_ <: java.util.Set[N]]) => paintNodes(colouriseWithHighlights[N](highlightedColorisation, highlighted, painter), nodes)

  def paintWithColourisation[N](
    painter: N => Expression[ColorisableGraphicalContent],
    nodes: Expression[_ <: Iterable[N]]) = 
      (colourisation: N => Expression[Colorisation]) => paintNodes(colourise(colourisation, painter), nodes)

  def colouriseWithHighlights[N](
    highlightedColorisation: Colorisation,
    highlighted: Expression[_ <: java.util.Set[_ <: N]],
    painter: N => Expression[ColorisableGraphicalContent]): N => Expression[GraphicalContent] = { (node: N) =>
    for (highlighted <- highlighted; painter <- painter(node))
      yield ColorisableGraphicalContent.Util.applyColourisation(painter, if (highlighted.contains(node)) highlightedColorisation else Colorisation.EMPTY)
  }

  def colourise[N](colorisation: N => Expression[Colorisation], painter: N => Expression[ColorisableGraphicalContent]): N => Expression[GraphicalContent] =
    (node: N) => for (p <- painter(node); c <- colorisation(node)) yield ColorisableGraphicalContent.Util.applyColourisation(p, c)

  def dontColourise[N](painter: N => Expression[ColorisableGraphicalContent]): N => Expression[GraphicalContent] = colouriseWithHighlights[N](Colorisation.EMPTY, constant(java.util.Collections.emptySet()), painter)
}