package org.workcraft.gui
import org.flexdock.perspective.PerspectiveManager
import org.flexdock.docking.DockingManager
import org.flexdock.perspective.persist.PerspectiveModel
import org.flexdock.perspective.persist.xml.XMLPersister
import java.io.File
import java.io.FileOutputStream
import java.io.FileNotFoundException
import java.io.FileInputStream
import java.awt.Container
import org.flexdock.docking.defaults.DefaultDockingPort
import java.awt.BorderLayout
import org.workcraft.logging.Logger
import org.workcraft.logging.Logger._
import scalaz.effects.IO
import scalaz.Scalaz._

class DockingLayoutSerialiser {
  val uiLayoutPath = "config/uilayout.xml"
  
  def saveDockingLayout(rootDockingPort:DefaultDockingPort) (implicit logger:() => Logger[IO]) {
    val pm = DockingManager.getLayoutManager().asInstanceOf[PerspectiveManager]
    pm.getCurrentPerspective().cacheLayoutState(rootDockingPort)
 //   pm.forceDockableUpdate()
    val pmodel = new PerspectiveModel(pm.getDefaultPerspective().getPersistentId(), pm.getCurrentPerspectiveName(), pm.getPerspectives())
    val pers = new XMLPersister()
    try {
      val file = new File(uiLayoutPath)
      val os = new FileOutputStream(file);
      pers.store(os, pmodel);
      os.close();
    } catch {
      case e: Throwable => (warning("Failed to save docking layout") *> warning(e)).unsafePerformIO
    }
  }

  def loadDockingLayout(rootDockingPort: DefaultDockingPort)(implicit logger:() => Logger[IO]) {
    val pm = DockingManager.getLayoutManager().asInstanceOf[PerspectiveManager]
    val pers = new XMLPersister()
    try {
      val f = new File(uiLayoutPath)
      if (!f.exists())
        return

      val is = new FileInputStream(f)

      val pmodel = pers.load(is)

      pm.remove("defaultWorkspace")
      pm.setCurrentPerspective("defaultWorkspace")

      pmodel.getPerspectives().foreach(pm.add(_, false))

   //   pm.reload(rootDockingPort)

      is.close()
    } catch {
      case e: Throwable => { (warning("Failed to load docking layout") *> warning(e)).unsafePerformIO }
    }
  }
}