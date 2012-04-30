
package org.workcraft.plugins.fsm

import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.gui.modeleditor.EditorService
import org.workcraft.scala.Expressions.Expression
import org.workcraft.scala.Expressions.ExpressionMonad
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.scala.Expressions.convertModifiableExpression
import org.workcraft.scala.Expressions.decorateExpression
import org.workcraft.scala.Expressions.monadicSyntaxV
import org.workcraft.scala.effects.IO.ioBind
import org.workcraft.scala.effects.IO.ioPure
import org.workcraft.scala.effects.IO
import org.workcraft.services.ModelService
import org.workcraft.services.Model
import org.workcraft.services.DefaultFormatService
import org.workcraft.services.Format
import org.workcraft.services.ModelScope
import org.workcraft.services.Service
import scalaz.Scalaz._
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.LayoutService
import org.workcraft.services.Layout

class FSMModel(val fsm: EditableFSM) extends ModelServiceProvider {
  def implementation[T](service: Service[ModelScope, T]) = service match {
    case EditorService => Some(new FSMEditor(fsm))
    case LayoutService => Some(FSMLayout(fsm))
    case _ => None
  }
}
