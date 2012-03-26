package org.workcraft.gui
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.JScrollPane
import org.workcraft.gui.propertyeditor.DisabledPanel
import javax.swing.ScrollPaneConstants
import org.workcraft.gui.modeleditor.tools.ModelEditorToolInstance
import scalaz._
import Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO

class ToolInterfaceWindow(interfacePanel : Expression[Option[JPanel]]) extends JPanel(new BorderLayout()) {
  private val content: JScrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  content.setBorder(null)
  content.setViewportView(new DisabledPanel())
  add(content, BorderLayout.CENTER)
  setFocusable(false)
  val refresh = swingAutoRefresh[Option[JPanel]](interfacePanel, p => IO.ioPure.pure {
    content.setViewportView(p.getOrElse(new DisabledPanel))
    content.revalidate
    })
}
