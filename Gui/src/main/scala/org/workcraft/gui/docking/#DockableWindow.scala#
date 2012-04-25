package org.workcraft.gui.docking

import org.flexdock.docking.defaults.AbstractDockable
import javax.swing.JComponent
import org.flexdock.docking.event.DockingEvent
import org.flexdock.docking.DockingPort
import javax.swing.JTabbedPane
import org.workcraft.gui.docking.tab.DockableTab
import org.flexdock.docking.DockingManager
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._

case class DockableWindowConfiguration[A <: JComponent](
  val closeButton: Boolean = true,
  val minimiseButton: Boolean = false,
  val maximiseButton: Boolean = true,
  val onMinimiseClicked: DockableWindow[A] => Unit = (_: DockableWindow[A]) => {},
  val onMaximiseClicked: DockableWindow[A] => Unit = (w: DockableWindow[A]) => w.toggleMaximised,
  val onCloseClicked: DockableWindow[A] => Unit = (w: DockableWindow[A]) => w.close)

class DockableWindow[A <: JComponent](
  val title: Expression[String],
  val persistentId: String,
  val content: A,
  val configuration: DockableWindowConfiguration[A]) extends AbstractDockable(persistentId) {

  val contentPanel = new DockableWindowContentPanel(this)

  def getComponent = contentPanel

  override val getDragSources = scala.collection.JavaConversions.asJavaList(List(contentPanel))

  def updateHeaders(port: DockingPort) {
    // T_T no better way of doing this since getDockables has no type
    val dockables = port.getDockables().toArray().toList.map(_.asInstanceOf[DockableWindow[A]])

    dockables.foreach(dockable => {
      (dockable.getComponent().getParent(), dockable.isMaximised) match {
        case (tabbedPane: JTabbedPane, false) => {
          dockable.hideHeader

          Range(0, tabbedPane.getComponentCount).find(dockable.getComponent() == tabbedPane.getComponentAt(_)).
            foreach(tabbedPane.setTabComponentAt(_, new DockableTab(dockable)))
        }

        case _ => { dockable.showHeader }
      }
    })
  }

  override def dockingComplete(evt: DockingEvent) = {
    updateHeaders(evt.getNewDockingPort())
    super.dockingComplete(evt)
  }

  override def undockingComplete(evt: DockingEvent) = {
    updateHeaders(evt.getOldDockingPort())
    super.undockingComplete(evt)
  }

  def hideHeader = contentPanel.hideHeader
  def showHeader = contentPanel.showHeader

  var isMaximised = false
  var isClosed = DockingManager.isDocked(this)

  def toggleMaximised = { DockingManager.toggleMaximized(this); isMaximised = !isMaximised; updateHeaders(getDockingPort) }
  def close = { DockingManager.close(this); isClosed = true }
  def display { DockingManager.display(this); isClosed = false }

  def ensureTabSelected = {
    (getComponent().getParent(), isMaximised) match {
      case (tabbedPane: JTabbedPane, false) => {

        Range(0, tabbedPane.getComponentCount).find(getComponent() == tabbedPane.getComponentAt(_)).
          foreach(tabbedPane.setSelectedIndex(_))
      }

      case _ => {}
    }
  }
}
