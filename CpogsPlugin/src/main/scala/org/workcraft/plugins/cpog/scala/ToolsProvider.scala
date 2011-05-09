
import org.workcraft.dom.visual.HitMan

import org.workcraft.plugins.cpog.CPOG
import org.workcraft.plugins.cpog.gui.Generators._
import org.workcraft.plugins.cpog.gui.TouchableProvider._

import org.workcraft.Tool
import java.awt.geom.Point2D
import pcollections.HashTreePSet;
import pcollections.PSet;
import org.workcraft.util.Maybe
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.GlobalCache.eval
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression

import org.workcraft.util.Function.Util.composition
import org.workcraft.util.Function
import org.workcraft.util.Function2
import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.connections._
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.gui.graph.tools.HitTester
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.tools.GraphEditorToolUtil._

import java.awt.Color
import java.awt.BasicStroke

import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.gui.graph.tools.Colorisation.{EMPTY => emptyColorisation}
import org.workcraft.gui.graph.tools.ConnectionController
import org.workcraft.gui.graph.tools.ConnectionTool
import org.workcraft.gui.graph.tools.selection.MoveDragHandler
import org.workcraft.gui.graph.tools.NodeGeneratorTool

import org.workcraft.dom.visual.ColorisableGraphicalContent.Util._
import org.workcraft.dom.visual.DrawMan;

import org.workcraft.gui.graph.tools.selection.GenericSelectionTool

import org.workcraft.gui.graph.Viewport
import java.lang.Boolean

import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.HitTester.hitTester
import org.workcraft.plugins.cpog.scala.NodePainter
import org.workcraft.gui.graph.tools.GraphEditorConfiguration
import org.workcraft.plugins.cpog.scala.MovableController

import scala.collection.JavaConversions._
import java.awt.geom.AffineTransform
import scala.collection.immutable.Set

package org.workcraft.plugins.cpog.scala {

import org.workcraft.plugins.cpog.CpogConnectionManager
import Expressions._
import Util._

  object ToolsProvider {

    val visualConnectionProperties = new VisualConnectionProperties {
      override def getDrawColor = Color.green
      override def getArrowWidth = 0.1
      override def getArrowLength = 0.2
      override def hasArrow = true
      override def getStroke = new BasicStroke(0.05f)
    }

    def selectionTool (
        selection: ModifiableExpression[PSet[Node]], 
        nodes : Expression[_ <: Iterable[Node]], 
        snap: Point2D => Point2D, 
        touchable: Node => Expression[Touchable],
        painter: Node => Expression[ColorisableGraphicalContent]
        ) = {
      val dragHandler = new MoveDragHandler[Node](selection, MovableController.position(_:Node), snap);
      
      val genericSelectionTool = new GenericSelectionTool[Node](selection, hitTester(nodes, touchable), dragHandler);
      
      val selectionTool = new AbstractTool {
        override def mouseListener = genericSelectionTool.getMouseListener
        override def userSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = genericSelectionTool.userSpaceContent(viewport)
        override def screenSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = constant(GraphicalContent.EMPTY)
        override def getButton = org.workcraft.gui.graph.tools.selection.SelectionTool.identification
      }
      
      attachPainter(selectionTool, drawWithHighlight[Node](highlightedColorisation, genericSelectionTool.effectiveSelection(), painter, nodes))
    }

	val highlightedColorisation = new Colorisation {
		override def getColorisation = new Color(99, 130, 191).brighter()
		override def getBackground = null
	}
    
    def drawWithHighlight[N](highlightedColorisation : Colorisation, highlighted : Expression[_ <: java.util.Set[_ >: N]] , painter : N => Expression[ColorisableGraphicalContent], nodes : Expression[_ <: Iterable[N]]) : Expression[GraphicalContent] = {
    	val colorisedPainter = (node : N) => for(highlighted <- highlighted; painter <- painter(node)) yield applyColourisation(painter, if (highlighted.contains(node)) highlightedColorisation else Colorisation.EMPTY)

    	for (nodes <- nodes;
    	    graphics <- joinCollection (nodes.map(colorisedPainter)))
    	yield graphics.foldLeft(GraphicalContent.EMPTY)(Graphics.compose)
    }
    
    def getTools(cpog: CPOG, snap: Function[Point2D, Point2D]): java.lang.Iterable[GraphEditorTool] = {
      
      val selectionJ = cpog.storage.create[PSet[Node]](HashTreePSet.empty())
      val selection = for (selection <- selectionJ) yield asScalaSet (selection)
      val generators = createFor(cpog)
 

      val transform = MovableController.positionWithDefaultZero(_)
      val transformAffine = (n : Node) => { 
        for(point <-  transform(n)) yield AffineTransform.getTranslateInstance(point.getX(), point.getY());
      }
      val touchable = TouchableProvider.touchable(transformAffine)(_)
      val painter = NodePainter.nodeColorisableGraphicalContent (transformAffine)(_)
      
      val nodes = for(nodes <- cpog.nodes) yield asScalaIterable[Node](nodes)
      val components = for(components <- cpog.components) yield asScalaIterable[Component](components)
      val selTool = selectionTool(selectionJ, nodes, (x => snap (x)), touchable, painter)
/*      val transformedComponentTouchableProvider = transform(componentLocalTouchable, asFunctionObject(transformer))

      val arcToGuiV: Arc => Expression[ConnectionGui] = arcToGui(transformedComponentTouchableProvider(_))

      val getTouchable = (x: ConnectionGui) => (x.shape)
      val getTouchableFunc = asFunctionObject(getTouchable)

      val nodeTouchableProvider = NodeVisitor.create(
        composition[Arc, Expression[ConnectionGui], Expression[Touchable]](
          asFunctionObject(arcToGuiV), javafmap[ConnectionGui, Touchable](getTouchableFunc)), transformedComponentTouchableProvider)

      
      val nv = new NodeVisitor[Expression[_ <: ColorisableGraphicalContent]] {
        override def visitArc(arc: Arc) = bindFunc(arcToGuiV(arc))(x => x.graphicalContent);
        override def visitComponent(component: Component) = {
          val bcgc = component.accept(new ComponentVisitor[Expression[_ <: BoundedColorisableGraphicalContent]] {
            override def visitRho(rho: RhoClause) = VisualRhoClause.getVisualRhoClause(rho)
            override def visitVariable(variable: Variable) = VisualVariableGui.getImage(variable)
            override def visitVertex(vertex: Vertex) = VisualVertex.image(vertex)
          })

          bindFunc(bindFunc(bcgc, componentMovableController(component))(BoundedColorisableGraphicalContent.translate(_, _)))(x => x.graphics)
        }
      }

      def nodePainter(node: Node) = node.accept(nv)

      val highlightedColorisation = new Colorisation {
        override def getColorisation = new Color(99, 130, 191).brighter()
        override def getBackground = null
      }

*/
      val connectionHitTester = hitTester(components, touchable)

      val connectionManager = ConnectionController.Util.fromSafe(new CpogConnectionManager(cpog));

      val connectionTool = new ConnectionTool[Component](MovableController.position(_:Component), connectionManager, connectionHitTester.hitTest(_:Point2D))

      val uncolorised = drawWithHighlight[Node](Colorisation.EMPTY, constant(java.util.Collections.emptySet()), painter, nodes)

      val controlPointsEditorTool = ControlPoints.gogo(selection, uncolorised)
      
      asJavaCollection (selTool ::
        controlPointsEditorTool ::
        attachPainter(connectionTool, 
            drawWithHighlight[Node](highlightedColorisation, for(node <- connectionTool.mouseOverNode()) yield java.util.Collections.singleton[Node](node), painter, nodes)) ::
        attachPainter(new NodeGeneratorTool(generators.vertexGenerator, snap), uncolorised) ::
        attachPainter(new NodeGeneratorTool(generators.variableGenerator, snap), uncolorised) ::
        attachPainter(new NodeGeneratorTool(generators.rhoClauseGenerator, snap), uncolorised) ::
      Nil)
    }
  }
}
