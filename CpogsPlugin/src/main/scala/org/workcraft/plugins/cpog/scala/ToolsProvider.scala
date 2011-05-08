
import org.workcraft.dom.visual.HitMan

import org.workcraft.plugins.cpog.CPOG
import org.workcraft.plugins.cpog.gui.Generators._
import org.workcraft.plugins.cpog.gui.TouchableProvider._

import org.workcraft.Tool
import java.awt.geom.Point2D
import pcollections.HashTreePSet;
import pcollections.PSet;
import org.workcraft.util.Maybe
import org.workcraft.dependencymanager.advanced.core.Expressions.{ fmap => javafmap, bind => javabind, asFunction, constant }
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
import java.lang.Iterable

import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.gui.graph.tools.ConnectionController
import org.workcraft.gui.graph.tools.ConnectionTool
import org.workcraft.gui.graph.tools.selection.MoveDragHandler
import org.workcraft.gui.graph.tools.NodeGeneratorTool

import org.workcraft.dom.visual.ColorisableGraphicalContent.Util._
import org.workcraft.dom.visual.DrawMan;

import org.workcraft.gui.graph.tools.selection.GenericSelectionTool

import org.workcraft.gui.graph.Viewport
import java.util.Set
import java.lang.Boolean

import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.HitTester.hitTester
import org.workcraft.plugins.cpog.scala.NodePainter
import org.workcraft.gui.graph.tools.GraphEditorConfiguration
import org.workcraft.plugins.cpog.scala.MovableController

import _root_.scala.collection.JavaConversions._

package org.workcraft.plugins.cpog.scala {

import java.awt.geom.AffineTransform
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
        nodes : Expression[List[Node]], 
        snap: Point2D => Point2D, 
        transform: Node => Expression[AffineTransform]
        
        ) = {
      val dragHandler = new MoveDragHandler[Node](selection, MovableController.position(_:Node), snap);
      
      val genericSelectionTool = new GenericSelectionTool[Node](selection, hitTester(nodes, touchable), dragHandler);
      
      val selectionTool = new AbstractTool {
        override def mouseListener = genericSelectionTool.getMouseListener
        override def userSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = genericSelectionTool.userSpaceContent(viewport)
        override def screenSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = constant(GraphicalContent.EMPTY)
        override def getButton = org.workcraft.gui.graph.tools.selection.SelectionTool.identification
      }
      
      attachPainter(selectionTool, )
    }

    def getTools(cpog: CPOG, snap: Function[Point2D, Point2D]): Iterable[GraphEditorConfiguration] = {
      
      
      val colourisablePainter = NodePainter.graphicalContent()
      
      
  /*    val selection = cpog.storage.create[PSet[Node]](HashTreePSet.empty())
      val generators = createFor(cpog)

      val transformedComponentTouchableProvider = transform(componentLocalTouchable, asFunctionObject(transformer))

      val arcToGuiV: Arc => Expression[ConnectionGui] = arcToGui(transformedComponentTouchableProvider(_))

      val getTouchable = (x: ConnectionGui) => (x.shape)
      val getTouchableFunc = asFunctionObject(getTouchable)

      val nodeTouchableProvider = NodeVisitor.create(
        composition[Arc, Expression[ConnectionGui], Expression[Touchable]](
          asFunctionObject(arcToGuiV), javafmap[ConnectionGui, Touchable](getTouchableFunc)), transformedComponentTouchableProvider)

      
      val connectionHitTester = createHitTester(cpog.components(), transformedComponentTouchableProvider)

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

      def makePainter(highlighted: Expression[_ <: Set[_ >: Node]]) = {

        def getColorisation(highlighted: Set[_ >: Node])(node: Node) = if (highlighted.contains(node))
          highlightedColorisation else Colorisation.EMPTY
        val coloriser = bindFunc(highlighted)(hl => getColorisation(hl)(_))

        def colorisedPainter(node: Node) = {
          bind(nodePainter(node), (gc: ColorisableGraphicalContent) => {
            bindFunc(coloriser)((coloriser: Node => Colorisation) => {
              applyColourisation(gc, coloriser(node))
            })
          })
        }

        def someshit(nodes: Iterable[_ <: Node]) = DrawMan.drawCollection(nodes, asFunctionObject(colorisedPainter))

        bind(
          cpog.nodes(), (nodes: Iterable[_ <: Node]) => someshit(nodes));
      };

      val connectionManager = ConnectionController.Util.fromSafe(new CpogConnectionManager(cpog));

      

      val selectionTool = new AbstractTool {
        override def mouseListener = genericSelectionTool.getMouseListener
        override def userSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = genericSelectionTool.userSpaceContent(viewport)
        override def screenSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = constant(GraphicalContent.EMPTY)
        override def getButton = org.workcraft.gui.graph.tools.selection.SelectionTool.identification
      }

      val connectionTool = new ConnectionTool[Component](componentMovableController, connectionManager, HitTester.Util.asPointHitTester(connectionHitTester))

      val painter = makePainter(constant(java.util.Collections.emptySet()))

      asJavaList(attachPainter(selectionTool, makePainter(genericSelectionTool.effectiveSelection())) ::
        attachPainter(connectionTool, makePainter(bindFunc(connectionTool.mouseOverNode())(java.util.Collections.singleton[Node](_)))) ::
        attachPainter(new NodeGeneratorTool(generators.vertexGenerator, snap), painter) ::
        attachPainter(new NodeGeneratorTool(generators.variableGenerator, snap), painter) ::
        attachPainter(new NodeGeneratorTool(generators.rhoClauseGenerator, snap), painter) ::
        Nil)*/
      Nil
    }
  }
}