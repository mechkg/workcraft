package org.workcraft.gui.docking
import javax.swing.JPanel
import javax.swing.JButton
import java.awt.Dimension
import javax.swing.Icon
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.UIManager
import java.awt.FlowLayout
import javax.swing.JLabel
import java.awt.Font
import javax.swing.JComponent
import javax.swing.BorderFactory
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._

class DockableWindowContentPanel[A <: JComponent](val window: DockableWindow[A]) extends JPanel {
  object Header extends JPanel {
    def createHeaderButton(icon: Icon) = {
      val button = new JButton()

      button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()))
      button.setFocusable(false)
      button.setBorder(null)
      button.setIcon(icon)

      button
    }

    setLayout(new BorderLayout())

    val c = if (UIManager.getLookAndFeel().getName().contains("Substance")) {
      val bgc = getBackground()
      new Color((bgc.getRed() * 0.9).toInt, (bgc.getGreen() * 0.9).toInt, (bgc.getBlue() * 0.9).toInt)
    } else
      UIManager.getColor("InternalFrame.activeTitleBackground")

    setBackground(c)

    if (window.configuration.minimiseButton || window.configuration.maximiseButton || window.configuration.closeButton) {
      val buttonPanel = new JPanel()
      buttonPanel.setBackground(c)
      buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 4, 2))
      buttonPanel.setFocusable(false)
      add(buttonPanel, BorderLayout.EAST)

      var buttons = 0

      if (window.configuration.minimiseButton) {
        val btnMin = createHeaderButton(UIManager.getIcon("InternalFrame.minimizeIcon"))
        btnMin.addActionListener(new ActionListener() {
          override def actionPerformed(evt: ActionEvent) = window.configuration.onMinimiseClicked(window)
        })

        btnMin.setToolTipText("Toggle minimized")
        buttonPanel.add(btnMin)
        buttons += 1
      }

      if (window.configuration.maximiseButton) {
        val btnMax: JButton = createHeaderButton(UIManager.getIcon("InternalFrame.maximizeIcon"))

        btnMax.addActionListener(new ActionListener() {
          override def actionPerformed(evt: ActionEvent) = {
            window.configuration.onMaximiseClicked(window)
            if (window.isMaximised) {
              btnMax.setIcon(UIManager.getIcon("InternalFrame.minimizeIcon"))
              btnMax.setToolTipText("Restore window")
            } else {
              btnMax.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"))
              btnMax.setToolTipText("Maximize window")
            }
          }
        })

        buttonPanel.add(btnMax)
        buttons += 1
      }

      if (window.configuration.closeButton) {
        val btnClose = createHeaderButton(UIManager.getIcon("InternalFrame.closeIcon"))
        btnClose.addActionListener(new ActionListener() {
          override def actionPerformed(evt: ActionEvent) = window.configuration.onCloseClicked(window)
        })
        btnClose.setToolTipText("Close window")
        buttonPanel.add(btnClose)
        buttons += 1
      }

      buttonPanel.setPreferredSize(new Dimension((UIManager.getIcon("InternalFrame.closeIcon").getIconWidth() + 4) * buttons, UIManager.getIcon("InternalFrame.closeIcon").getIconHeight() + 4))
    }

    val label = new JLabel(window.title.unsafeEval)

    val refresh = swingAutoRefresh (window.title, (title:String) => ioPure.pure { label.setText(title) })

    label.setOpaque(false);
    label.setForeground(UIManager.getColor("InternalFrame.activeTitleForeground"));
    label.setFont(label.getFont().deriveFont(Font.BOLD));

    add(label, BorderLayout.WEST);
  }

  setLayout(new BorderLayout(0, 0))

  val contentPane = new JPanel()
  contentPane.setLayout(new BorderLayout(0, 0))
  contentPane.add(window.content, BorderLayout.CENTER)
  contentPane.setBorder(BorderFactory.createLineBorder(contentPane.getBackground(), 2))

  contentPane.add(Header, BorderLayout.NORTH)

  add(contentPane, BorderLayout.CENTER)

  setFocusable(false)

  def showHeader =
    if (Header.getParent() != contentPane) {
      contentPane.add(Header, BorderLayout.NORTH)
      contentPane.doLayout()
    }

  def hideHeader =
    if (Header.getParent() == contentPane) {
      contentPane.remove(Header)
      contentPane.doLayout()
    }
}
