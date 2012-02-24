package org.workcraft.graphics

import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._

object GraphicsHelper {
  def paintNodes[N](painter: N => Expression[GraphicalContent], nodes: Expression[_ <: Iterable[N]]) =
    for (
      nodes <- nodes;
      graphics <- nodes.map(painter).toList.sequence
    ) yield graphics.foldLeft(GraphicalContent.Empty)(_.compose(_))

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
    for (highlighted <- highlighted; cgc <- painter(node))
      yield cgc.applyColorisation(if (highlighted.contains(node)) highlightedColorisation else Colorisation.Empty)
  }

  def colourise[N](colorisation: N => Expression[Colorisation], painter: N => Expression[ColorisableGraphicalContent]): N => Expression[GraphicalContent] =
    (node: N) => for (cgc <- painter(node); c <- colorisation(node)) yield cgc.applyColorisation(c)

  def dontColourise[N](painter: N => Expression[ColorisableGraphicalContent]): N => Expression[GraphicalContent] = colouriseWithHighlights[N](Colorisation.Empty, constant(java.util.Collections.emptySet()), painter)
}