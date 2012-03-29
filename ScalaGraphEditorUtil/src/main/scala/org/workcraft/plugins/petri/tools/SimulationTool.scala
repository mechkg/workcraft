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
import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._
import org.workcraft.swing.Swing
import org.workcraft.swing.Swing._
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.gui.modeleditor.tools.DummyMouseListener
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.Modifier
import org.workcraft.gui.modeleditor.KeyBinding
import org.workcraft.gui.modeleditor.KeyEventType
import org.workcraft.gui.GUI
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.Button
import org.workcraft.scala.effects.IO
import org.workcraft.graphics.Colorisation
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.gui.modeleditor.tools.ModelEditorToolInstance

case class SimColors(fg: Color, bg: Color)


object SimulationTool {

  lazy val button = new Button {
        def label = "Simulation"
        def hotkey = Some(KeyEvent.VK_M)
        def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/start-green.svg").unsafePerformIO)
      }
  
  def apply[Event](simControl: SimControl[Swing, Event], hitTester: Point2D.Double => Swing[Option[Event]], colors: IO[SimColors]): 
  (ToolEnvironment => ModelEditorToolInstance, Event => IO[Colorisation]) =
    (env =>
    new ModelEditorToolInstance {

      override def keyBindings = List(
        KeyBinding("Unfire", KeyEvent.VK_OPEN_BRACKET, KeyEventType.KeyTyped, Set.empty, simControl.unfire.unsafeRun >| None),
        KeyBinding("Fire", KeyEvent.VK_CLOSE_BRACKET, KeyEventType.KeyTyped, Set.empty, (simControl.getNextEvent >>= ((e: Event) => simControl.fire(e))).unsafeRun >| None))

      override def mouseListener = Some(new DummyMouseListener {
        override def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double) =
          (hitTester(position) >>= (_.traverse_(x => simControl.fire(x)))).unsafeRun
      })

      override def screenSpaceContent: Expression[GraphicalContent] = env.hasFocus >>= {
        case true => GUI.editorMessage(env.viewport, Color.BLACK, "Simulation: click on the highlighted transitions to fire them")
        case false => constant[GraphicalContent](GraphicalContent.Empty)
      }


      override def interfacePanel = None



      override def userSpaceContent : Expression[GraphicalContent] = constant(GraphicalContent.Empty)
    }, (event : Event) => {
      def mkColorisation(col: Color, back: Color) = new Colorisation(Some(col), Some(back))
      val nextTransitionColorisation = colors map { case SimColors(fg, bg) => mkColorisation(bg, fg) }
      val enabledTransitionColorisation = colors map { case SimColors(fg, bg) => mkColorisation(fg, bg) }
        simControl.getNextEvent.unsafeRun >>= (nextEvent => 
        if (event == nextEvent)
          nextTransitionColorisation
        else simControl.canFire(event).unsafeRun >>= {
          case true => enabledTransitionColorisation
          case false => IO.ioPure.pure(Colorisation.Empty)
        })
      })
}
