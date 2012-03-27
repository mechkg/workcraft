package org.workcraft.gui.propertyeditor
/*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import org.workcraft.gui.MainWindow
import javax.swing.WindowConstants
import scala.swing.event.ActionEvent

class SettingsEditorDialog(owner: MainWindow) extends JDialog(owner) {

  private val propertiesTable: PropertyEditorTable = new PropertyEditorTable
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  setModal(true)
  setTitle("Settings")
  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) = ok
  })
  var parentSize: Dimension = owner.getSize()
  this.setSize(parentSize.width / 2, parentSize.height / 2)
  var mySize: Dimension = getSize()
  owner.getLocationOnScreen()
  this.setLocation((parentSize.width - mySize.width) / 2 + 0, (parentSize.height - mySize.height) / 2 + 0)
  initComponents()
  loadSections()
  private var contentPane: JPanel = null
  private var propertiesPane: JPanel = null
  private var buttonsPane: JPanel = null
  private var okButton: JButton = null
  private var sectionScroll: JScrollPane = null
  private var sectionRoot: DefaultMutableTreeNode = null
  private var sectionTree: JTree = null

  def getSectionNode(node: DefaultMutableTreeNode, section: String): DefaultMutableTreeNode = {
    var dotPos: Int = section.indexOf('.')
    var thisLevel: String = null
    var nextLevel: String = null
    if (dotPos < 0) {
      thisLevel = section
      nextLevel = null
    } else {
      thisLevel = section.substring(0, dotPos)
      nextLevel = section.substring(dotPos + 1)
    }

    var thisLevelNode: DefaultMutableTreeNode = null

    {
      var i: Int = 0
      while (i < node.getChildCount()) {
        var child: DefaultMutableTreeNode = node.getChildAt(i).asInstanceOf[DefaultMutableTreeNode]
        if (child.getUserObject().isInstanceOf[String]) {

          if (child.getUserObject().asInstanceOf[String].equals(thisLevel)) {
            thisLevelNode = child
            error("break")
          }

          i = i + 1
        }
      }
    }

    if (thisLevelNode == null)
      thisLevelNode = new DefaultMutableTreeNode(thisLevel)

    node.add(thisLevelNode)
    if (nextLevel == null)
      return thisLevelNode
    else
      return getSectionNode(thisLevelNode, nextLevel)

  }

  private def addItem(section: String, item: SettingsPage): Unit = {
    val sectionNode: DefaultMutableTreeNode = getSectionNode(sectionRoot, section)
    sectionNode.add(new DefaultMutableTreeNode(new SettingsPageNode(item)))
  }

  private def loadSections(): Unit = {
    /*for (val info <- framework.getPluginManager().getPlugins(classOf[SettingsPage])) {
      var e:SettingsPage = info.getSingleton()
      addItem(e.getSection(), e)
    }*/
    sectionTree.setModel(new DefaultTreeModel(sectionRoot))
  }

  def clearObject = propertiesTable.clearObject

  def setObject(p: SettingsPage) = propertiesTable.setObject(p.getProperties)

  private def initComponents(): Unit = {
    contentPane = new JPanel(new BorderLayout())
    setContentPane(contentPane)
    sectionScroll = new JScrollPane()
    sectionRoot = new DefaultMutableTreeNode("root")
    sectionTree = new JTree()
    sectionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
    sectionTree.setRootVisible(false)
    sectionTree.setShowsRootHandles(true)
    sectionTree.addTreeSelectionListener(new TreeSelectionListener {
      override def valueChanged(e: TreeSelectionEvent) {
        val userObject = (e.getPath.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode]).getUserObject;
        userObject match {
          case obj: SettingsPageNode => setObject(obj.getPage);
          case _ => clearObject
        }
      }
    })
    sectionScroll.setViewportView(sectionTree)
    sectionScroll.setMinimumSize(new Dimension(200, 0))
    sectionScroll.setPreferredSize(new Dimension(200, 0))
    sectionScroll.setBorder(BorderFactory.createTitledBorder("Section"))
    propertiesPane = new JPanel()
    propertiesPane.setBorder(BorderFactory.createTitledBorder("Selection properties"))
    propertiesPane.setLayout(new BorderLayout())
    propertiesPane.add(propertiesTable)
    buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10))
    okButton = new JButton()
    okButton.setPreferredSize(new Dimension(100, 20))
    okButton.setText("OK")
    okButton.addActionListener(new java.awt.event.ActionListener {
      override def actionPerformed(e: java.awt.event.ActionEvent) = ok
    })
    buttonsPane.add(okButton)
    contentPane.add(sectionScroll, BorderLayout.WEST)
    contentPane.add(propertiesPane, BorderLayout.CENTER)
    contentPane.add(buttonsPane, BorderLayout.SOUTH)
    getRootPane().setDefaultButton(okButton)
  }

  private def ok(): Unit = {
    setObject(null)
    setVisible(false)
  }
  class SettingsPageNode(page: SettingsPage) {
    override def toString(): String = {
      return page.getName
    }

    def getPage(): SettingsPage = {
      return page
    }
  }
}

object SettingsEditorDialog {
  private val serialVersionUID: Long = 1L
}
  */