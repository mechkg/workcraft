package org.workcraft.plugins.petri.tools

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

import javax.swing.JButton
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer

import org.workcraft.swing.Swing
import Swing._

import SimulationControlPanel._

import scalaz._
import Scalaz._

class SimulationTraceTable[State]
  ( model : SimulationModel[Swing, String, State]
  , cellRenderer : (String, Boolean) => Component
  ) {
  val traceTable = create
  val loadFromClipboardButton = new JButton("from Clipb")
  val saveToClipboardButton = new JButton("to Clipb")
  saveToClipboardButton.addActionListener(new ActionListener {
      override def actionPerformed(e : ActionEvent) = TracesClipboardOwner.saveToClipboard(traces)
    })

  loadFromClipboardButton.addActionListener(new ActionListener {
    override def actionPerformed(e : ActionEvent) {
      traces = TracesClipboardOwner.loadFromClipboard
    }
  })

  var traces : TracePair = TracePair.createEmpty

  def getCurrentTrace : TraceStep = {
    if(traces.branch.trace.list.isEmpty)
      traces.main
    else
      traces.branch
  }

  def getSimControl : SimulationControl[Swing, SimulationState[State]] = new SimulationModel[Swing, Unit, SimulationState[State]]() {
    override def canFire(event : Unit) = unsafeToSwing {
      val TraceStep(trace, step) = getCurrentTrace
      step < trace.size && model.canFire(trace(step)).unsafePerformIO
    }
    override def fire(event : Unit) = unsafeToSwing(quietStep)
    override def canUnfire(event : Unit) = unsafeToSwing {
      val TraceStep(trace, step) = getCurrentTrace
      step > 0 && model.canUnfire(trace(step-1)).unsafePerformIO
    }
    override def unfire(event : Unit) = unsafeToSwing {
      quietStepBack
    }
    override def saveState : Swing[SimulationState[State]] = unsafeToSwing(new SimulationState[State](model.saveState.unsafePerformIO, traces))
    override def loadState(state : SimulationState[State]) = unsafeToSwing {
      model.loadState(state.modelState).unsafePerformIO
      traces = state.traces
    }
  }

  def components : List[Component] = List ( 
	  traceTable
          , saveToClipboardButton
          , loadFromClipboardButton
	  )

  def quietStepBack : Boolean = {
    def go(lens : Lens[TracePair, TraceStep]) : Boolean = {
      val TraceStep(trace, step) = lens.get(traces)
      val event = trace(step - 1)

      if(!model.canUnfire(event).unsafePerformIO)
	false
      else {
	model.unfire(event).unsafePerformIO
      
	traces = (TraceStep.step compose lens).mod(traces, _ - 1)
	true
      }
    }

    if (traces.branch.step > 0) {
      val res = go(TracePair.branch)
      
      if (res && traces.branch.step == 0 && !traces.main.trace.isEmpty)
	traces = (TraceStep.trace compose TracePair.branch).set(traces, Trace(Nil))

      res
    } else {
      if (traces.main.step == 0)
	false
      else 
	go(TracePair.main)
    }
  }

  def quietStep : Boolean = {
    if(!traces.branch.trace.isEmpty) { // If we have a branch-trace, use that. Otherwise, use the main trace.
      if (traces.branch.step < traces.branch.trace.size) {
	val event = traces.branch.trace(traces.branch.step)
    
	if(!model.canFire(event).unsafePerformIO) {println("can't fire " + event) ; 
	  return false}
	model.fire(event).unsafePerformIO

	traces = (TraceStep.step compose TracePair.branch).mod(traces, _ + 1)
	
	return true
      }
	else
	  return false
    }  else {
      if (traces.main.step == traces.main.trace.size)
	return false
	  
      val event = traces.main.trace(traces.branch.step)
	  
      if(!model.canFire(event).unsafePerformIO)
	return false
      model.fire(event).unsafePerformIO
	  
      traces = (TraceStep.step compose TracePair.main).mod(traces, _ + 1)
      return true
    }
  }
	
    def userRequestedFire(event : String) = {
      val nextEvent = getNextEvent
      println("firing " + event + " (next event : " + nextEvent + ")")
      if (!event.equals(nextEvent)) {
	traces = TracePair.branch.mod(traces, branch => {
	  TraceStep(Trace(branch.trace.list.take(branch.step) :+ event), branch.step)
	})
      }
      quietStep
    }

    def create : JTable = {
      val traceTable = new JTable(new AbstractTableModel {
	override def getColumnCount = 2

	override def getColumnName(column : Int) : String = {
	  if (column == 0)
	    return "Trace"
	  return "Branch"
	}
	override def getRowCount = {
	  val tnum = traces.main.trace.size
	  val bnum = traces.branch.trace.size

	  Math.max(tnum, bnum + traces.main.step)
	}

	override def getValueAt(row : Int, col : Int) : Object = {
	  if (col == 0) {
	    if (!traces.main.trace.isEmpty && row < traces.main.trace.size)
	      return traces.main.trace(row)
	  } else {
	    if (!traces.branch.trace.isEmpty && row >= traces.main.step && row < traces.main.step + traces.branch.trace.size) {
	      return traces.branch.trace(row - traces.main.step)
	    }
	  }
	  ""
	}
      })
		
      traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

      traceTable.addMouseListener(new MouseListener {
	override def mouseClicked(e : MouseEvent) = {
	  val column = traceTable.getSelectedColumn
	  val row = traceTable.getSelectedRow

	  if (column == 0) {
	    if (row < traces.main.trace.size) {

	      var work = true
	      
	      while (traces.branch.step > 0 && work)
  		work = quietStepBack

	      while (traces.main.step > row && work)
		work = quietStepBack

	      while (traces.main.step < row && work)
		work = quietStep
	    }
	  } else {
	    if (traces.branch.trace != null && row >= traces.main.step && row < traces.main.step + traces.branch.trace.size) {
	      var work = true

	      while (traces.main.step + traces.branch.step > row && work)
		work = quietStepBack

	      while (traces.main.step + traces.branch.step < row && work)
		work = quietStep
	    }
	  }
	}
	override def mouseEntered(arg0 : MouseEvent) = {}
	override def mouseExited(arg0 : MouseEvent) = {}
	override def mousePressed(arg0 : MouseEvent) = {}
	override def mouseReleased(arg0 : MouseEvent) = {}
      })

      traceTable.setDefaultRenderer(classOf[Object], new TableCellRenderer {
	def isActive(row : Int, column : Int) : Boolean = {
	  if (column == 0) {
	    if (traces.branch.trace.isEmpty)
	      return row == traces.main.step
	  } else {
	    if (row >= traces.main.step && row < traces.main.step + traces.branch.trace.size) {
	      return (row - traces.main.step) == traces.branch.step
	    }
	  }
	  
	  return false
	}

	override def getTableCellRendererComponent(table : JTable, value : Object, isSelected : Boolean, hasFocus : Boolean, row : Int, column : Int) : Component = value match {
	  case str : String => cellRenderer.apply(str, isActive(row, column))
	  case _ => null
	}
      })
      traceTable
    }

    def setTraces(traces : TracePair) = {
      this.traces = traces
    }


    def getNextEvent : String = {
      val TraceStep(t, i) = getCurrentTrace
      if(t.size == i)
	null
      else
	t(i)
    }
	
    def asSimControl : SimControl[Swing, String] = {
      new SimControl[Swing, String] {
	override def unfire = unsafeToSwing(SimulationTraceTable.this.quietStepBack)
	override def getNextEvent : Swing[String] = unsafeToSwing(SimulationTraceTable.this.getNextEvent)
	override def fire(event : String) = unsafeToSwing(userRequestedFire(event))
	override def canFire(event : String) : Swing[Boolean] = model.canFire(event)
      }
    }
}
