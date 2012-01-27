package org.workcraft.gui

import java.awt.Font
import javax.swing.JFrame
import org.workcraft.services.GlobalServiceManager
import org.workcraft.logging.Logger
import javax.swing.JDialog
import javax.swing.UIManager
import org.pushingpixels.substance.api.SubstanceLookAndFeel
import org.pushingpixels.substance.api.SubstanceConstants.TabContentPaneBorderKind

class MainWindow(val globalServices: GlobalServiceManager) extends JFrame {
  setTitle("Workcraft")
  setSize(800, 600)

  JDialog.setDefaultLookAndFeelDecorated(true)
  UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL)

  String laf = framework.getConfigVar("gui.lookandfeel")
  if (laf == null)
    laf = UIManager.getCrossPlatformLookAndFeelClassName()
  LAF.setLAF(laf);
  SwingUtilities.updateComponentTreeUI(this)

  content = new JPanel(new BorderLayout(0, 0))
  setContentPane(content);

}