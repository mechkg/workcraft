
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.Color

import scala.collection.JavaConversions.asJavaCollection
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.immutable.Set

import org.workcraft.dom.visual.connections.VisualConnectionProperties
import org.workcraft.graphics.GraphicsHelper
import org.workcraft.gui.graph.tools.GraphEditorToolUtil.attachPainter
import org.workcraft.gui.graph.tools.ConnectionController
import org.workcraft.gui.graph.tools.NodeGeneratorTool
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.plugins.cpog.scala.GeneratorTools
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.scala.BooleanFormulaInstances._
import org.workcraft.plugins.cpog.scala.nodes.Arc
import org.workcraft.plugins.cpog.scala.nodes.Component
import org.workcraft.plugins.cpog.scala.nodes.Node
import org.workcraft.plugins.cpog.scala.nodes.RhoClause
import org.workcraft.plugins.cpog.scala.nodes.Variable
import org.workcraft.plugins.cpog.scala.nodes.Vertex
import org.workcraft.scala.grapheditor.tools.ConnectionTool
import org.workcraft.plugins.cpog.scala.tools.ControlPointsTool
import org.workcraft.plugins.cpog.scala.tools.MovableController
import org.workcraft.scala.grapheditor.tools._
import org.workcraft.plugins.cpog.scala.CpogConnectionManager
import org.workcraft.scala.Expressions.ExpressionMonad
import org.workcraft.scala.Expressions.monadicSyntax
import org.workcraft.scala.Scalaz.SeqMA
import org.workcraft.scala.Scalaz.maImplicit
import org.workcraft.scala.Util.asFunctionObject
import org.workcraft.scala.Util.javaCollectionToList
import org.workcraft.util.Function

import pcollections.PVector
import pcollections.HashTreePSet
import pcollections.PSet
import pcollections.TreePVector

package org.workcraft.plugins.cpog.scala {

import org.workcraft.scala.Expressions._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._

  case class ToolsProvider(cpog : CPOG) {

    val visualConnectionProperties = new VisualConnectionProperties {
      override def getDrawColor = Color.green
      override def getArrowWidth = 0.1
      override def getArrowLength = 0.2
      override def hasArrow = true
      override def getStroke = new BasicStroke(0.05f)
    }
    
    val varsWithNames : Expression[List[(Variable, String)]] = 
      cpog.variables.flatMap[List[(Variable,String)]](
        vars => {
          val qwe : List[Expression[(Variable, String)]] = (javaCollectionToList(vars)
            .map(v =>
              (for (label <- v.visualProperties.label: Expression[String])
                yield (v, label))));
          val res : Expression[List[(Variable, String)]] = qwe.sequence[Expression, (Variable, String)]
          res
        })
    
    // This would be needed if there were "encoding" properties
    val sortedVariables = {
      for (varsWithNames <- varsWithNames)  
    	yield scala.util.Sorting.stableSort(varsWithNames, (a : (Variable, String), b : (Variable, String)) => {
    	  val (_, la) = a
    	  val (_, lb) = b
    	  la < lb
    	}).map({case (v, _) => v})
    }
    
    val varByName = for(vars <- varsWithNames) yield (name : String) => (for((v, n) <- vars if n equals name) yield v).toList match { case v :: Nil => v } 
    
    def formulaAsString (formula : ModifiableExpression[BooleanFormula[Variable]]) : ModifiableExpression[String] = ModifiableExpression (
    	for(formula <- formula : Expression[BooleanFormula[Variable]];
    		formula <- (for(v <- formula) yield v.visualProperties.label : Expression[String]).sequence)
    		yield
    	  FormulaToString.print(formula)
    	, (str : String) => {
    		val strFormula = BooleanParser.parse(str)
    	  	val varByNameV = eval(varByName)
    	  	formula.setValue(VariableReplacer.replace((n : String) => varByNameV(n), strFormula))
    	  }
    )
    
    
    def getProperties(node : Node) : PVector[EditableProperty] = {
      node match {
        case v : Variable => VisualVariable.getProperties(v)
        case v : Vertex => TreePVector.empty[EditableProperty].plus(StringProperty.create("Condition", formulaAsString(v.condition).jexpr))//VisualVertex.getProperties(v)
        case r : RhoClause => TreePVector.empty[EditableProperty].plus(StringProperty.create("Condition", formulaAsString(r.formula).jexpr))//VisualRhoClause.getProperties(r)
        case a : Arc => TreePVector.empty[EditableProperty].plus(StringProperty.create("Condition", formulaAsString(a.condition).jexpr))
      }
    }
    
    private val selectionJ = cpog.storage.create[PSet[Node]](HashTreePSet.empty())
    private val selection : Expression[Set[Node]] = for (selection <- selectionJ) yield Set.apply(javaCollectionToList(selection) : _*)
    private val generators = GeneratorTools(cpog)
 

    private val transform = MovableController.positionWithDefaultZero(_)
    private val transformAffine = (n : Node) => { 
        for(point <-  transform(n)) yield AffineTransform.getTranslateInstance(point.getX(), point.getY());
      }
    private val touchable : Node => Expression[org.workcraft.dom.visual.Touchable] = TouchableProvider.touchable(transformAffine)(_:Node)
    private val painter : Node => Expression[org.workcraft.dom.visual.ColorisableGraphicalContent] = NodePainter.nodeColorisableGraphicalContent (transformAffine)(_)
      
    private val nodes = for(nodes <- cpog.nodes) yield collectionAsScalaIterable[Node](nodes)
    private val components = for(components <- cpog.components) yield collectionAsScalaIterable[Component](components)
      
    val properties : Expression[PVector[EditableProperty]] = for(selection <- selection) yield selection.toList match {
        case node :: Nil => getProperties(node)
        case _ => TreePVector.empty[EditableProperty]
      }
    
    def selectedArcs (selection: Expression[Set[Node]]): Expression[List[(Point2D, Point2D, VisualArc)]] =
      for (selection <- selection;
           result <-
           {
             val list = for (arc@Arc(first,second,_,visual) <- selection) yield
                           for (first <- first.visualProperties.position; second<- second.visualProperties.position; visual <- visual)
                             yield (first, second, visual)
             list.toList.sequence
           }
          ) yield result
        
    
    def tools(snap: Function[Point2D, Point2D]) = {
      def paintUncolorised = GraphicsHelper.paint(painter, nodes)
      def paintWithHighlights = GraphicsHelper.paintWithHighlights(painter, nodes)
      
      val selectionTool = SelectionTool.create[Node](nodes, selectionJ, MovableController.position(_), (x => snap (x)), touchable)
      
      val connectionController = ConnectionController.Util.fromSafe(new CpogConnectionManager(cpog))
      
      val connectionTool = ConnectionTool.create[Component](components, touchable, MovableController.positionWithDefaultZero(_), connectionController)
      
      val controlPointsTool = ControlPointsTool.create(selectedArcs(selection), (x=>snap(x)))

      //val controlPointsEditorTool = ControlPoints.gogo(selection, uncolorised)

      asJavaCollection (
          selectionTool.asGraphEditorTool(paintWithHighlights) ::
          connectionTool.asGraphEditorTool(paintWithHighlights) ::
          controlPointsTool.asGraphEditorTool(paintUncolorised) ::
        attachPainter(new NodeGeneratorTool(generators.vertexGenerator, snap), paintUncolorised) ::
        attachPainter(new NodeGeneratorTool(generators.variableGenerator, snap), paintUncolorised) ::
        attachPainter(new NodeGeneratorTool(generators.rhoClauseGenerator, snap), paintUncolorised) ::
      Nil)
    
    }
  }
}
