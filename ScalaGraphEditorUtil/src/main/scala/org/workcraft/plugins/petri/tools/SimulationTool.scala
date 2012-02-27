/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 * 
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.petri.tools

import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.KeyEvent
import java.awt.geom.Point2D

import javax.swing.Icon
import javax.swing.JPanel

import org.workcraft.dependencymanager.advanced.core.{Expression => JExpression};

import org.workcraft.scala.Expressions._
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.gui.events.GraphEditorKeyEvent
import org.workcraft.gui.events.GraphEditorMouseEvent
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.gui.graph.tools.DecorationProvider
import org.workcraft.gui.graph.tools.GraphEditorTool.Button
import org.workcraft.util.GUI
import org.workcraft.gui.graph.tools.DummyKeyListener
import org.workcraft.gui.graph.tools.DummyMouseListener

import scalaz._
import Scalaz._

import org.workcraft.swing.Swing

case class SimColors (fg : Color, bg : Color)

class SimulationTool[Node, Event]
    ( simControl : SimControl[Swing, Event]
      , simStateControl : SimStateControl[Swing]
      , hitTester : Point2D => Option[Event]
      , nodeEventExtractor : Node => Option[Event]
      , interfacePanel : JPanel
      , colors : Expression[SimColors])
    extends AbstractTool {

    override def deactivated = simStateControl.reset.unsafePerformIO
    override def activated = simStateControl.rememberInitialState.unsafePerformIO

    override def keyListener = new DummyKeyListener {
      override def keyPressed(e : GraphEditorKeyEvent) = {
	if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET)
	  simControl.unfire.unsafePerformIO
	if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET)
	  simControl.fire(simControl.getNextEvent.unsafePerformIO).unsafePerformIO
      }
    }

    override def mouseListener = new DummyMouseListener {
      override def mousePressed(e : GraphEditorMouseEvent) = 
	hitTester(e.getPosition()) map (simControl.fire(_).unsafePerformIO)
    }

    override def screenSpaceContent(view : Viewport, hasFocus : JExpression[java.lang.Boolean]) : JExpression[_ <: GraphicalContent] = (Expression(hasFocus) >>= (focused => 
     if(focused) GUI.editorMessage(view, Color.BLACK, "Simulation: click on the highlighted transitions to fire them")
     else constant[GraphicalContent](GraphicalContent.EMPTY)
    )).jexpr

    override def getButton = new Button {
      def getLabel = "Simulation"
      def getHotKeyCode = KeyEvent.VK_M
      def getIcon = GUI.createIconFromSVG("images/icons/svg/start-green.svg")
    }

    override def getInterfacePanel = interfacePanel

    def mkColorisation (col : Color, back : Color) = new Colorisation {
      override def getColorisation = col
      override def getBackground = back
    }

    val nextTransitionColorisation = colors map { case SimColors(fg, bg) => (bg, fg) }
    val enabledTransitionColorisation = colors map { case SimColors(fg, bg) => (fg, bg) }
    
    // TODO: make it somehow register dependency on the enabledness
    def getColorisation(node : Node) = nodeEventExtractor(node) match {
      case Some(event) => {
    	val nextEvent = simControl.getNextEvent.unsafePerformIO
    	if(event.equals(nextEvent))
    	  nextTransitionColorisation
    	else if(simControl.canFire(event).unsafePerformIO)
    	  enabledTransitionColorisation
    	else constant(Colorisation.EMPTY)
      }
      case None => constant(Colorisation.EMPTY)
    }

    override def userSpaceContent(viewport : Viewport, hasFocus : JExpression[java.lang.Boolean]) : JExpression[_ <: GraphicalContent] = constant(GraphicalContent.EMPTY).jexpr
}
