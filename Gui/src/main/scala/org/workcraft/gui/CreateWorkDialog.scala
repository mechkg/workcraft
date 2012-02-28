package org.workcraft.gui;

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.WindowConstants
import java.awt.event.MouseAdapter
import java.awt.Window
import javax.swing.JOptionPane
import org.workcraft.services.NewModelImpl

class CreateWorkDialog private (models: List[NewModelImpl]) extends JDialog {
  class ListElement(val newModel: NewModelImpl) {
    override def toString = newModel.name
  }

  var choice: Option[NewModelImpl] = None

  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  setModal(true)
  setTitle("New work")

  val contentPane = new JPanel(new BorderLayout())
  setContentPane(contentPane)

  val modelScroll = new JScrollPane()
  val listModel = new DefaultListModel()

  val modelList = new JList(listModel)
  modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  modelList.setLayoutOrientation(JList.VERTICAL_WRAP)

  modelList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
    def valueChanged(e: javax.swing.event.ListSelectionEvent) =
      if (modelList.getSelectedIndex() == -1)
        okButton.setEnabled(false);
      else okButton.setEnabled(true);
  })

  modelList.addMouseListener(new MouseAdapter() {
    override def mouseClicked(e: MouseEvent) {
      if (e.getClickCount() == 2)
        if (modelList.getSelectedIndex() != -1)
          create()
    }
  })

  models.sortBy(_.name).foreach(d => listModel.addElement(new ListElement(d)))

  modelScroll.setViewportView(modelList)
  modelScroll.setBorder(BorderFactory.createTitledBorder("Type"))

  val optionsPane = new JPanel()
  optionsPane.setBorder(BorderFactory.createTitledBorder("Options"))
  optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS))

  val chkVisual = new JCheckBox("Create visual model")
  chkVisual.setSelected(true)

  val chkOpen = new JCheckBox("Open in editor")
  chkOpen.setSelected(true)

  optionsPane.add(chkVisual)
  optionsPane.add(chkOpen)
  optionsPane.add(new JLabel("Model title: "))
  val txtTitle = new JTextField()
  optionsPane.add(txtTitle)

  val dummy = new JPanel()
  dummy.setPreferredSize(new Dimension(200, 1000));
  dummy.setMaximumSize(new Dimension(200, 1000));

  optionsPane.add(dummy)

  val buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10))

  val okButton = new JButton()
  okButton.setPreferredSize(new Dimension(100, 20))
  okButton.setEnabled(false)
  okButton.setText("OK")
  okButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      if (okButton.isEnabled())
        create()
    }
  })

  val cancelButton = new JButton()
  cancelButton.setPreferredSize(new Dimension(100, 20))
  cancelButton.setText("Cancel")
  cancelButton.addActionListener(new java.awt.event.ActionListener() {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      cancel()
    }
  })

  buttonsPane.add(okButton)
  buttonsPane.add(cancelButton)

  contentPane.add(modelScroll, BorderLayout.CENTER)
  // contentPane.add(optionsPane, BorderLayout.WEST)
  contentPane.add(buttonsPane, BorderLayout.SOUTH)

  txtTitle.addKeyListener(new java.awt.event.KeyAdapter() {
    override def keyPressed(e: java.awt.event.KeyEvent) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER)
        if (okButton.isEnabled())
          create()
    }
  })

  def cancel() = {
    choice = None
    setVisible(false)
  }

  def create() = {
    choice = modelList.getSelectedValue match {
      case e: ListElement => Some(e.newModel)
      case _ => None
    }
    setVisible(false)
  }

  def createVisual = chkVisual.isSelected

  def openInEditor = chkOpen.isSelected
}

object CreateWorkDialog {
  def show(models: List[NewModelImpl], parentWindow: Window): Option[NewModelImpl] = {
    if (models.isEmpty) {
      JOptionPane.showMessageDialog(parentWindow, "Workcraft was unable to find any plug-ins that could create a new model.\n\nReconfiguring Workcraft (Utility->Reconfigure) might fix this.\n\nIf you are running Workcraft from a development environment such as Eclipse,\nplease make sure to add the plug-in classes to the classpath in run configuration. ", "Warning", JOptionPane.WARNING_MESSAGE)
      None
    } else {
      val dialog = new CreateWorkDialog(models)
      GUI.centerAndSizeToParent(dialog, parentWindow)
      dialog.setVisible(true)
      dialog.choice
    }
  }
}