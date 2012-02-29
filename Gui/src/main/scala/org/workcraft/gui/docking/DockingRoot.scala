package org.workcraft.gui.docking
import javax.swing.JPanel
import org.flexdock.perspective.PerspectiveManager
import org.flexdock.docking.DockingManager
import org.flexdock.perspective.Perspective
import org.flexdock.docking.defaults.DefaultDockingPort
import java.awt.BorderLayout
import javax.swing.JComponent
import org.flexdock.docking.DockingConstants
import org.flexdock.docking.defaults.StandardBorderManager
import org.flexdock.plaf.common.border.ShadowBorder

class DockingRoot(val id: String) extends JPanel {
  val pm = DockingManager.getLayoutManager().asInstanceOf[PerspectiveManager]
  pm.add(new Perspective(id, id))
  pm.setCurrentPerspective(id, true)
  
  val rootDockingPort = new DefaultDockingPort("root")
  
  rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()))

  setLayout(new BorderLayout)
  add(rootDockingPort, BorderLayout.CENTER)
  
  def createWindow(title: String, persistentId: String, content: JComponent, configuration: DockableWindowConfiguration,
      neighbour: DockableWindow, relativeRegion: String = DockingConstants.CENTER_REGION, split: Double = 0.5) = {
    val window = new DockableWindow(title, persistentId, content, configuration)
    DockingManager.registerDockable(window)
    DockingManager.dock(window, neighbour, relativeRegion)
    DockingManager.setSplitProportion(window, split.toFloat)
    
    window
  }
  
  def createRootWindow(title: String, persistentId: String, content: JComponent, configuration: DockableWindowConfiguration) = {
    val window = new DockableWindow(title, persistentId, content, configuration)
    DockingManager.registerDockable(window)
    DockingManager.dock(window, rootDockingPort, DockingConstants.CENTER_REGION)
    
    window
  }
}