package org.workcraft.plugins.stg.tools

import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.util.Maybe.Util._
import java.awt.Color
import java.awt.Component
import java.awt.geom.Point2D
import javax.swing.JLabel
import javax.swing.JPanel
import org.workcraft.dom.visual.HitMan
import org.workcraft.gui.SimpleFlowLayout
import org.workcraft.plugins.petri.tools.SimControl
import org.workcraft.plugins.petri.tools.SimStateControl
import org.workcraft.plugins.petri.tools.SimulationControlPanel
import org.workcraft.plugins.petri.tools.SimulationState
import org.workcraft.plugins.petri.tools.SimulationTool
import org.workcraft.plugins.petri.tools.SimulationTraceTable
import org.workcraft.util.Hierarchy
import org.workcraft.plugins.stg21.StgModel
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.plugins.stg21.types.Id
import org.workcraft.plugins.stg21.types.ExplicitPlace
import org.workcraft.plugins.stg21.types.Transition
import org.workcraft.plugins.stg21.types._
import org.workcraft.plugins.stg21.parsing.Place
import org.workcraft.plugins.petri.tools.SimulationModel
import org.workcraft.swing.Swing
import org.workcraft.swing.Swing._
import org.workcraft.scala.Expressions._
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.plugins.stg21.types.MathStg
import scalaz._
import Scalaz.ma
import Scalaz.mab
import Scalaz.maImplicit
import Scalaz.mkIdentity
import Semigroup._
import Scalaz.alphas
import org.workcraft.plugins.stg21.parsing.ExplicitPlacePlace
import org.workcraft.plugins.stg21.types.ConsumingArc
import org.workcraft.plugins.stg21.types.ImplicitPlaceArc
import org.workcraft.plugins.stg21.parsing.ImplicitPlace
import org.workcraft.plugins.stg21.types.ProducingArc
import org.workcraft.plugins.stg21.types.DummyLabel
import org.workcraft.plugins.stg21.types.SignalLabel
import org.workcraft.plugins.stg21.types.TransitionLabel
import org.workcraft.scala.effects.IO
import org.workcraft.plugins.stg21.types.SignalType
import org.workcraft.plugins.petri.tools.SimColors
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.plugins.stg21.types.VisualStg
import org.workcraft.plugins.stg21.StgVisualStuff
import org.workcraft.scala.grapheditor.tools.HitTester
import org.workcraft.gui.modeleditor.tools.ModelEditorToolInstance
import org.workcraft.gui.modeleditor.tools.EmptyModelEditorToolInstance
import org.workcraft.gui.CommonVisualSettings

object Sim {
  def getTableCellRenderer(net: MathStg): (String, Boolean) => Component = (transitionName, isActive) => {
    val label = new JLabel
    label.setOpaque(true)
    label.setForeground(Color.BLACK)

    label.setText(transitionName)

    val fore = null // Color.GREEN //eval(PetriNetSettings.enabledForegroundColor)
    val back = null // Color.BLUE // eval(PetriNetSettings.enabledBackgroundColor)

    if (isActive) {
      if (fore != null && back != null) {
        label.setBackground(fore)
        label.setForeground(back)
      } else {
        label.setBackground(Color.YELLOW)
      }
    } else {
      label.setBackground(Color.WHITE)
    }

    SimHelper.transitionByName(net, transitionName) match {
      case (t :: _) => net.transitions(t)._1 match {
      case SignalLabel(sig, _) => net.signals(sig).direction match {
        case SignalType.Input => label.setForeground(inputsColor)
        case SignalType.Output => label.setForeground(outputsColor)
        case SignalType.Internal => label.setForeground(internalsColor)
        case _ => {}
      }
      case _ => {}
    }
      case _ => {}
    }

    

    label
  }

  def createSimulationTool(visualStg: VisualStg): IO[ToolEnvironment => ModelEditorToolInstance] = IO.ioPure.pure {
    val stg = visualStg.math
    val marking: Variable[Marking] = Variable.create(stg.initialMarking)
    val model = new StgSimulationModel(stg, marking)
    val traceTable = new SimulationTraceTable[Marking](model, getTableCellRenderer(stg))
    val simControl = traceTable.asSimControl
    val controlPanel = new SimulationControlPanel[SimulationState[Map[Place, Int]]](traceTable.getSimControl)
    val simStateControl = controlPanel.asStateControl

    val interfacePanel_ = new JPanel(new SimpleFlowLayout(5, 5))

    for (controlComponent <- controlPanel.components ++ traceTable.components)
      interfacePanel_.add(controlComponent)

    import StgVisualStuff._

    val hitTester = marking.eval map (m => HitTester.create(
      visualStg.math.transitions.keys.filter { k => SimHelper.fire(visualStg.math, k, m).isDefined },
      (transId: Id[Transition]) => new StgVisualStuff(visualStg).touchable(NodeVisualEntity(StgVisualNode(TransitionNode(transId))))(null)))

    val (toolStub, colorisation) = SimulationTool[String](
      simControl,
      p => liftIO(hitTester.map(h => h.hitTest(p).map(t => SimHelper.transitionName(stg, t)))),
      IO.ioPure.pure(SimColors(Color.GREEN, Color.BLUE)))
    env => toolStub(env) |+| new EmptyModelEditorToolInstance{
      override def interfacePanel = Some(interfacePanel_)
      override def userSpaceContent = (CommonVisualSettings.settings.expr <**> marking) ((s, m) => visualStg.copy(math = visualStg.math.setInitialMarking(m)).image(s))
    }
  }

  type Marking = Map[Place, Int]
  val inputsColor = Color.RED.darker
  val outputsColor = Color.BLUE.darker
  val internalsColor = Color.GREEN.darker

  class StgSimulationModel(stg: MathStg, marking: Variable[Marking]) extends SimulationModel[Swing, String, Marking] {

    override def canFire(event: String): Swing[Boolean] = liftIO {
      marking.eval map { marking =>
        !(SimHelper.transitionByName(stg, event) flatMap (t => SimHelper.fire(stg, t, marking))).isEmpty
      }
    }

    override def fire(event: String) = liftIO {
      marking.update(mrk => (SimHelper.transitionByName(stg, event) flatMap (t => SimHelper.fire(stg, t, mrk))) match {
        case List(m) => m
        case _ => throw new RuntimeException("there is no unique fireable transition with name '" ++ event ++ "'")
      })
    }

    override def canUnfire(event: String) = liftIO {
      marking.eval map { marking =>
        !(SimHelper.transitionByName(stg, event) flatMap (t => SimHelper.unfire(stg, t, marking))).isEmpty
      }
    }
    override def unfire(event: String) = liftIO {
      marking.update(mrk => (SimHelper.transitionByName(stg, event) flatMap (t => SimHelper.unfire(stg, t, mrk))) match {
        case List(m) => m
        case _ => throw new RuntimeException("there is no unique fireable transition with name '" ++ event ++ "'")
      })
    }

    override def saveState: Swing[Map[Place, Int]] = liftIO(marking.eval)

    override def loadState(m: Map[Place, Int]) = liftIO(marking.set(m))
  }

}

object SimHelper {

  def showLabel(stg: MathStg, tl: TransitionLabel) = tl match {
    case DummyLabel(s) => s
    case SignalLabel(sigId, dir) => stg.signals(sigId).name + dir
  }

  def transitionName(stg: MathStg, t: Id[Transition]): String = {
    val tr = stg.transitions(t)
    showLabel(stg, tr._1) + (if (tr._2 == 0) "" else "/" + tr._2)
  }

  def transitionByName(stg: MathStg, s: String): List[Id[Transition]] =
    stg.transitions.keys >>= { k => if (transitionName(stg, k) == s) List(k) else List() }

  def preset(stg: MathStg, t: Id[Transition]): Marking = {
    (stg.arcs.map.toList >>= {
      case (_, ConsumingArc(from, to)) if to == t => List(ExplicitPlacePlace(from))
      case (arcId, arc @ ImplicitPlaceArc(from, to, _)) if to == t => List(ImplicitPlace(arcId.downCast))
      case _ => List()
    }).groupBy(p => p).map { case (p, ps) => (p, ps.length) }.toMap
  }

  def postset(stg: MathStg, t: Id[Transition]): Marking = {
    (stg.arcs.map.toList >>= {
      case (_, ProducingArc(from, to)) if from == t => List(ExplicitPlacePlace(to))
      case (arcId, arc @ ImplicitPlaceArc(from, to, _)) if from == t => List(ImplicitPlace(arcId.downCast))
      case _ => List()
    }).groupBy(p => p).map { case (p, ps) => (p, ps.length) }.toMap
  }

  final type Marking = Map[Place, Int]

  def fireRaw(pre: Marking, post: Marking, marking: Marking): Option[Marking] =
    if (pre.toList.any { case (p, w) => marking.get(p).getOrElse(0) < w }) None
    else Some(marking |+| pre.mapValues(x => -x) |+| post)

  def fire(stg: MathStg, t: Id[Transition], marking: Marking): Option[Marking] =
    fireRaw(preset(stg, t), postset(stg, t), marking)

  def unfire(stg: MathStg, t: Id[Transition], marking: Marking) =
    fireRaw(postset(stg, t), preset(stg, t), marking)
}
