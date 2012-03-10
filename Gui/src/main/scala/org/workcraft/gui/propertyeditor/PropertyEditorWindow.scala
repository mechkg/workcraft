package org.workcraft.gui.propertyeditor

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import pcollections.PVector
import pcollections.TreePVector
import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._


class PropertyEditorWindow extends JPanel {
  private val propertyTable:PropertyEditorTable = new PropertyEditorTable
  private val scrollProperties:JScrollPane = new JScrollPane
  scrollProperties.setViewportView(propertyTable)
  setLayout(new BorderLayout(0, 0))
  add(new DisabledPanel(), BorderLayout.CENTER)
  validate()
  @SuppressWarnings(Array("unused"))
  private val refresher:AutoRefreshExpression = new AutoRefreshExpression {
			override def onEvaluate(context : EvaluationContext) = {
			    val obj = context.resolve(prop)
			    obj match {
					case None => clearObject
			        case Some(props) => setObject(props)
			    }
			}
  }
  val propertyObject:Variable[Expression[Option[List[EditableProperty]]]] = Variable.create(constant(None))
  private val prop:Expression[Option[List[EditableProperty]]] = (propertyObject : Expression[Expression[Option[List[EditableProperty]]]]).join

  def setObject(o:List[EditableProperty]):Unit = {
    removeAll()
    propertyTable.setObject(o)
    add(scrollProperties, BorderLayout.CENTER)
    validate()
    repaint()
  }

  def clearObject():Unit = {
    removeAll()
    propertyTable.clearObject()
    add(new DisabledPanel, BorderLayout.CENTER)
    validate()
    repaint()
  }
}
