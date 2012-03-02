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
* along with Workcraft.  If not, see [http://www.gnu.org/licenses/].
*
*/

package org.workcraft.gui.graph.tools

import org.workcraft.scala.Expressions._
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import javax.swing.Icon
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.gui.events.GraphEditorMouseEvent
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.GraphEditorTool.Button
import org.workcraft.util.GUI
import scalaz._
import Scalaz._
import org.workcraft.scala.Scalaz
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.=;
import org.workcraft.graphics.GraphicalContent
import java.awt.event.InputEvent

class GenericConnectionTool[N]
  (centerProvider : N => Expression[Point2D.Double], 
      connectionManager : ConnectionManager[N], 
      hitTester : Point2D.Double => Option[N]) {
  private val mouseOverObject : ModifiableExpression[Option[N]] = Variable.create[Option[N]](None)
  private val first : ModifiableExpression[Option[N]] = Variable.create[Option[N]](None)

  private var mouseExitRequiredForSelfLoop : Boolean = true
  private var leftFirst : Boolean = false
  private var lastMouseCoords : ModifiableExpression[Point2D.Double] = Variable.create(new Point2D.Double)
  private var warningMessage : ModifiableExpression[Option[String]] = Variable.create[Option[String]](None)
  
  def userSpaceContent(viewport : Viewport, hasFocus : Expression[Boolean]) : Expression[GraphicalContent] =
    connectingLineGraphicalContent(viewport)

  val mouseOverNode : Expression[Option[N]] = mouseOverObject
  val firstNode : Expression[Option[N]] = first
  
  private def connectingLineGraphicalContent(viewport : Viewport) : Expression[GraphicalContent] = 
    first >>= {
      case None => constant(GraphicalContent.Empty)
      case Some(first) => {
        warningMessage.setValue(None)
        mouseOverObject >>= {
          case None => constant(GraphicalContent.Empty)
          case Some(mouseOver) => {
            val color = connectionManager.connect(first, mouseOver) match {
              case Left(err) => { warningMessage.setValue(Some(err.getMessage)); Color.RED }
              case Right(_) => { Color.GREEN }
            }
            drawConnectingLine(color)
          }
        }
      }
    }
    
  private def drawConnectingLine(color : Color) : Expression[GraphicalContent] = {
    first >>= {
      case None => throw new RuntimeException("Should not happen!")
      case Some(first) => 
        for(
            center <- centerProvider(first)
            ; lastCoords <- lastMouseCoords) yield (
        new GraphicalContent{
          override def draw(g : Graphics2D) = {
            g.setColor(color)
            g.draw(new Line2D.Double(center.getX, center.getY, lastCoords.getX, lastCoords.getY))
          }
        })
      }
    }

  def mouseListener : GraphEditorMouseListener = new DummyMouseListener {
      override def mouseMoved(e : GraphEditorMouseEvent) = {
        lastMouseCoords.setValue(e.getPosition)
        
        val newMouseOverObject = hitTester.apply(e.getPosition)
        
        mouseOverObject.setValue(newMouseOverObject)

        if (!leftFirst && mouseExitRequiredForSelfLoop) {
          if (eval(mouseOverObject).equals(eval(first)))
            mouseOverObject.setValue(None)
          else
            leftFirst = true
        }
      }
      
      override def mousePressed(e : GraphEditorMouseEvent) = {
        if (e.getButton == MouseEvent.BUTTON1) {
          unsafeEval(first) match {
            case None => unsafeEval(mouseOverObject) match {
              case None => {}
              case Some(mouseOver) => {
                unsafeAssign(first, mouseOverObject)
                leftFirst = false
                mouseMoved(e)
              }
            }
            case Some(currentFirst) => {
              unsafeEval(mouseOverObject) match {
                case None => {}
                case Some(mouseOver) => {
                  connectionManager.connect(currentFirst, mouseOver) match {
                    case Right(connect) => {
                      connect
                      if ((e.getModifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
                          assign(first, mouseOverObject)
                          mouseOverObject.setValue(None)
                        } else {
                          first.setValue(None)
                        }
                    }
                    case Left(err) => Toolkit.getDefaultToolkit.beep
                  }
                }
              }
            }
          }
        } else if (e.getButton == MouseEvent.BUTTON3) {
          first.setValue(None)
          mouseOverObject.setValue(None)
        }
      }
      
  }

  def screenSpaceContent(viewport : Viewport, hasFocus : Expression[Boolean]) : Expression[GraphicalContent] = {
    (hasFocus >>= {
      case false => constant(GraphicalContent.Empty)
      case true => (warningMessage >>= {
        case Some(msg) => constant((Color.RED, msg))
        case None => first >>= {
          case None => constant((Color.BLACK, "Click on the first component"))
          case Some(_) => constant((Color.BLACK, "Click on the second component (control+click to connect continuously)"))
        }
      }).>>=(msg => GUI.editorMessage(viewport, msg._1, msg._2).map(x=>GraphicalContent(g => x.draw(g))))})
  }
  
  def deactivated = {
    first.setValue(None)
    mouseOverObject.setValue(None)
  }
}

object GenericConnectionTool {
  val button =
    new Button {
      override def getHotKeyCode = KeyEvent.VK_C
      override def getIcon = GUI.createIconFromSVG("images/icons/svg/connect.svg")
      override def getLabel ="Connection tool"
    }
}
