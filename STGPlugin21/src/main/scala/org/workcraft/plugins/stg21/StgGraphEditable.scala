package org.workcraft.plugins.stg21
import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.GraphEditable
import org.workcraft.gui.graph.tools.GraphEditor
import pcollections.PVector
import org.workcraft.gui.propertyeditor.EditableProperty
import pcollections.TreePVector
import org.workcraft.gui.modeleditor.tools.selection.GenericSelectionToolMouseListener
import org.workcraft.dependencymanager.advanced.user.Variable
import pcollections.HashTreePSet
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.DragHandle
import org.workcraft.gui.graph.tools.DragHandler
import org.workcraft.plugins.stg21.types._
import org.workcraft.scala.grapheditor.tools.GenericSelectionTool
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.stg.RichGraphicalContent
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.scala.grapheditor.tools.GenericConnectionTool
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.util.Action
import org.workcraft.dom.visual.connections.VisualConnectionProperties
import scalaz.Lens
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.graphics.Graphics
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.Colorisation
import RichGraphicalContent._
import org.workcraft.gui.modeleditor.ModelEditor
import scalaz.NonEmptyList
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.scala.grapheditor.tools.GenericConnectionTool
import org.workcraft.gui.modeleditor.tools._
import org.workcraft.gui.modeleditor.tools.ModelEditorTool.ModelEditorToolConstructor
import org.workcraft.scala.grapheditor.tools.HitTester
import org.workcraft.gui.CommonVisualSettings
import StgVisualStuff._
import org.workcraft.scala.effects.IO
import ModelEditorTool._
import org.workcraft.plugins.petri.tools.SimulationTool
import org.workcraft.gui.modeleditor.KeyBinding
import org.workcraft.gui.modeleditor.ToolMouseListener
import javax.swing.JPanel

class StgGraphEditable(visualStg : ModifiableExpression[VisualStg]) extends ModelEditor {
  val undo = None
  val selection = Variable.create[Set[VisualEntity]](Set.empty)
  
  def tools: NonEmptyList[ModelEditorTool2] = {

    def movableController(n : VisualNode) : ModifiableExpression[Point2D.Double] = {
      import org.workcraft.plugins.stg21.modifiable._
      n match {
        case StgVisualNode(node) =>
          {
            val n = visualStg.visual.nodesInfo.lookup(node)
            n.orElse(VisualInfo(position = new Point2D.Double(1, 0), parent = None)).position
          }
        case GroupVisualNode(g) => {
          visualStg.visual.groups.lookup(g).refract(Lens((x : Option[Group]) => x match {
            case None => new Point2D.Double(0,0)
            case Some(g) => g.info.position
          },
          (o : Option[Group], x : Point2D.Double) => o match {
            case None => None
            case Some(g) => Some(g.copy(info=g.info.copy(position=x)))
          }))
        }
      }
    }
    
    def deepCenters(n : StgConnectable) : Expression[Point2D.Double] = n match {
      case NodeConnectable(n) => movableController(StgVisualNode(n))
      case ArcConnectable(a) => constant(new Point2D.Double(0,0)) // TODO
    }
    
    val connectionController = new StgConnectionManager(visualStg)
    
    val env = visualStg.expr <|*|> CommonVisualSettings.settings
    
    def connectableTouchable(c : StgConnectable) : Expression[TouchableC] = env.map{case (vstg, s) => c match { // TODO: deep!
      case NodeConnectable(n) => vstg.touchableC(NodeVisualEntity(StgVisualNode(n)))(s)
      case ArcConnectable(a) => vstg.touchableC(ArcVisualEntity(a))(s)
    }}
    
    def paint : Expression[GraphicalContent] = env.map{ case (vstg, s) => {
      vstg.visualEntities.map(vstg.visual(_)(s).bcgc.cgc).
      foldl(ColorisableGraphicalContent.Empty)(_.compose(_)).applyColorisation(Colorisation.Empty)
    }
    }

    val connectionTool = GenericConnectionTool.apply[StgConnectable](visualStg.map(_.stgConnectables), (connectableTouchable(_)).map(_.map(_.touchable)), deepCenters, connectionController, _ => paint)
    
    val nodeGeneratorTools = org.workcraft.plugins.stg21.StgToolsProvider(visualStg).nodeGeneratorTools(paint)
    
    import modifiable._
    
    val selectionTool = GenericSelectionTool.apply[VisualEntity](
        visualStg.map(_.visualEntities),
        selection, 
        n => entityPosition(n).map(l => visualStg.refract(l)),
        (x => /*snap */x),
        n => env.map({case (v, s) => v.touchable(n)(s)}),
        (_ => paint),
        Nil)
    
    implicit def decorateMET2A(a : ModelEditorTool2Activation) = new {
      def attachPainter(p : GraphicalContent) : ModelEditorTool2Activation = new ModelEditorTool2Activation {
  def keyBindings: List[KeyBinding] = a.keyBindings
  def mouseListener: Option[ToolMouseListener] = a.mouseListener
  def userSpaceContent: Expression[GraphicalContent] = a.userSpaceContent.map (c => p compose c) 
  def screenSpaceContent: Expression[GraphicalContent] = a.screenSpaceContent
  def interfacePanel: Option[JPanel] = a.interfacePanel
      }
    }
        
        
    val simulationTool = new ModelEditorTool2 {
      val button = SimulationTool.button
      def activate (env : ToolEnvironment) : IO[ModelEditorTool2Activation] = {
        (visualStg.eval >>= (v => 
          paint.eval >>= (paint => 
            org.workcraft.plugins.stg.tools.Sim.createSimulationTool(v).map(s => 
              s(env).attachPainter(paint)))))
      }
    }
    
    NonEmptyList.nel(
      selectionTool,
      ModelEditorTool.as2(connectionTool) ::
      (nodeGeneratorTools.map(x => ModelEditorTool.as2(x)) ++ List(simulationTool)))
  }

  def props : Expression[List[Expression[EditableProperty]]] = {
    for(s <- (selection : Expression[Set[VisualEntity]]);
      props <- s.toList.traverse(e => EditableProperties.objProperties(e)(visualStg))
    ) yield (props.flatten)
  }
}

object StgGraphEditable {
  def hitTest(vstg : VisualStg) : HitTester[VisualEntity] = HitTester.create(vstg.visualEntities, (n : VisualEntity) => vstg.touchable(n)(null))
}
