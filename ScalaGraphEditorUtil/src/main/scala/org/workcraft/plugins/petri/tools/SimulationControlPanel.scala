package org.workcraft.plugins.petri.tools

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JSlider
import javax.swing.Timer
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import org.workcraft.swing.Swing
import org.workcraft.swing.Swing._
import SimulationControlPanel._
import org.workcraft.gui.GUI

import scalaz.Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

object SimulationControlPanel {
  type SimulationControl[M[_], State] = SimulationModel[M, Unit, State] {
  }
}


// unsafe class -- all the methods including the constructor are side-effectful
class SimulationControlPanel[State](simControl : SimulationControl[Swing, State]) {


  val resetButton = new JButton("Reset")
  val speedSlider = new JSlider(-1000, 1000, 0)
  val autoPlayButton = (GUI.createIconFromSvg("images/icons/svg/start.svg", 16, 16, None) >>= (GUI.createIconButton(_, "Automatic simulation"))).unsafePerformIO
  val stopButton = new JButton("Stop")
  val backButton = new JButton("Step <")
  val stepButton = new JButton("Step >")
  val loadTraceButton = new JButton("Load trace")
  val saveMarkingButton = new JButton("Save marking")
  val loadMarkingButton = new JButton("Load marking")
  speedSlider.addChangeListener(new ChangeListener {
    override def stateChanged(e : ChangeEvent) {
      timer match {
	case Some(t) => {
	  t.stop
	  t.setInitialDelay(getAnimationDelay)
	  t.setDelay(getAnimationDelay)
	  t.start
	}
	case _ => {}
      }
      update
    }
  })
  var initialState : State = simControl.saveState.unsafeRun.unsafePerformIO
  resetButton.addActionListener(
    new ActionListener { override def actionPerformed(e : ActionEvent) = reset })
		
  autoPlayButton.addActionListener(new ActionListener{
    override def actionPerformed(e : ActionEvent) {
      timer = Some(new Timer(getAnimationDelay, new ActionListener {
	override def actionPerformed(e : ActionEvent) {
	  simControl.fire(()).unsafeRun.unsafePerformIO
	}
      }))
      timer.map(_.start)
      update
    }
  })

  stopButton.addActionListener(new ActionListener {
    override def actionPerformed(e : ActionEvent) {
      timer.map(_.stop)
      timer = None
      update
    }
  });
  backButton.addActionListener(new ActionListener {
    override def actionPerformed(e : ActionEvent) {
      simControl.unfire(()).unsafeRun.unsafePerformIO
    }
  })
  stepButton.addActionListener(new ActionListener {
    override def actionPerformed(e : ActionEvent) {
      simControl.fire(()).unsafeRun.unsafePerformIO
    }
  })
  saveMarkingButton.addActionListener(new ActionListener {
    override def actionPerformed(e : ActionEvent) {
      savedState = Some(simControl.saveState.unsafeRun.unsafePerformIO)
    }
  })
  loadMarkingButton.addActionListener(new ActionListener {
    override def actionPerformed(e : ActionEvent) {
      savedState.map(s => simControl.loadState(s).unsafeRun.unsafePerformIO)
    }
  })
  var timer : Option[Timer] = None
  var savedState : Option[State] = None

  val DEFAULT_SIMULATION_DELAY = 0.3;
  val EDGE_SPEED_MULTIPLIER = 10;

  def getAnimationDelay : Int = {
    return (1000.0 * DEFAULT_SIMULATION_DELAY * scala.math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0)).toInt;
  }
	
  def components :  List[Component] =
                List( resetButton
                     , speedSlider
                     , autoPlayButton
                     , stopButton
                     , backButton
                     , stepButton
                     , loadTraceButton
                     , saveMarkingButton
                     , loadMarkingButton)
	
  def reset = simControl.loadState(initialState)
  def rememberInitialState = { initialState = simControl.saveState.unsafeRun.unsafePerformIO }
	
  def update = {
    val atEnd = !(simControl.canFire(()).unsafeRun.unsafePerformIO)
    val atStart = !(simControl.canUnfire(()).unsafeRun.unsafePerformIO)
    
    timer match {
      case Some(t) => {
	if(atEnd) {
	  t.stop
	  timer = None
	} else {
	  t.setDelay(getAnimationDelay)
	}
      }
      case None => {}
    }

    resetButton.setEnabled(!atStart)
    autoPlayButton.setEnabled(!atEnd)
    stopButton.setEnabled(timer != null)
    
    backButton.setEnabled(!atStart)

    stepButton.setEnabled(!atEnd)

    loadTraceButton.setEnabled(true)

    saveMarkingButton.setEnabled(true)
    loadMarkingButton.setEnabled(savedState.isDefined)
  }

  def asStateControl : SimStateControl[Swing] = new SimStateControl[Swing] {
    override def reset = unsafeToSwing(SimulationControlPanel.this.reset)
    override def rememberInitialState = unsafeToSwing(SimulationControlPanel.this.rememberInitialState)
  };
}
