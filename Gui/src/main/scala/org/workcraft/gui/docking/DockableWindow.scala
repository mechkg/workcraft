package org.workcraft.gui.docking
import org.flexdock.docking.defaults.AbstractDockable
import javax.swing.JComponent
import org.flexdock.docking.event.DockingEvent
import org.flexdock.docking.DockingPort
import javax.swing.JTabbedPane
import org.workcraft.gui.docking.tab.DockableTab

case class DockableWindowConfiguration(
  val closeButton: Boolean = true,
  val minimiseButton: Boolean = true,
  val maximiseButton: Boolean = true, 
  val onMinimiseClicked: () => Unit = () => {},
  val onMaximiseClicked: () => Unit = () => {},
  val onCloseClicked: () => Unit = () => {}    
)

class DockableWindow(
  val title: String,
  val persistentId: String,
  val content: JComponent,
  val configuration: DockableWindowConfiguration
  ) extends AbstractDockable(persistentId) {

  val contentPanel = new DockableWindowContentPanel(this)

  def getComponent = contentPanel

  override val getDragSources = scala.collection.JavaConversions.asJavaList(List(contentPanel))

  def updateHeaders(port: DockingPort) {
    // T_T no better way of doing this since getDockables has no type
    val dockables = port.getDockables().toArray().toList.map(_.asInstanceOf[DockableWindow])

    dockables.foreach(dockable => {
      (dockable.getComponent().getParent(), dockable.isMaximised) match {
        case (tabbedPane: JTabbedPane, false) => {
          dockable.hideHeader

          Range(0, tabbedPane.getComponentCount).find(dockable.getComponent() == tabbedPane.getComponentAt(_)).
            foreach(tabbedPane.setTabComponentAt(_, new DockableTab(dockable)))
        }

        case _ => dockable.showHeader
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
}