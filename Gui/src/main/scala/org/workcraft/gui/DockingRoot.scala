package org.workcraft.gui
import javax.swing.JPanel
import org.flexdock.perspective.PerspectiveManager
import org.flexdock.docking.DockingManager
import org.flexdock.perspective.Perspective
import org.flexdock.docking.defaults.DefaultDockingPort
import java.awt.BorderLayout

class DockingRoot (val id: String) extends JPanel {
  val pm = DockingManager.getLayoutManager().asInstanceOf[PerspectiveManager]
  pm.add(new Perspective(id, id))
  pm.setCurrentPerspective(id, true)

  val rootDockingPort = new DefaultDockingPort("root")
  
  setLayout(new BorderLayout)
  add(rootDockingPort, BorderLayout.CENTER)
}