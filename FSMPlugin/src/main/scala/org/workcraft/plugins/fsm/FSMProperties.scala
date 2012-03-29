package org.workcraft.plugins.fsm
import org.workcraft.scala.Expressions.ModifiableExpressionWithValidation
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._
import scalaz._
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.gui.propertyeditor.bool.BooleanProperty
import org.workcraft.dependencymanager.advanced.user.Variable

case class FSMProperties(fsm: EditableFSM, selection: Expression[Set[Node]], pushUndo: String => IO[Unit]) {
  def name(p: State): ModifiableExpressionWithValidation[String, String] = {
    val expr = fsm.labels.map(_(p))
    ModifiableExpressionWithValidation(
      expr, name => if (PetriNet.isValidName(name)) {
        (
          for {
            oldName <- expr.eval;
            names <- fsm.stateNames.eval
          } yield if (name != oldName) {
            if (names.contains(name)) ioPure.pure(Some("The name '" + name + "' is already taken."))
            else {
              pushUndo("change state name") >>=|
                fsm.stateNames.set(names - oldName + ((name, p))) >>=|
                fsm.labels.update(_ + ((p, name))) >| None
            }
          } else
            ioPure.pure(None)).join
      } else ioPure.pure(Some("State names must be non-empty Latin alphanumeric strings not starting with a digit.")))
  }
  
  def initial(s: State): ModifiableExpression[Boolean] = 
    ModifiableExpression (fsm.initialState.map (_ == s), setInitial => {println (setInitial); if (setInitial) pushUndo("change initial state") >>=| fsm.initialState.set(s) else IO.Empty})
  
  def terminal(s: State): ModifiableExpression[Boolean] = 
    ModifiableExpression (fsm.finalStates.map (_.contains(s)), setTerminal => pushUndo("change final state") >>=| (if (setTerminal) fsm.finalStates.update(_ + s) else fsm.finalStates.update(_ - s)))
  
  def label(a: Arc) : ModifiableExpression[String] = ModifiableExpression (fsm.arcLabels.map(_(a)), label => pushUndo("change arc label") >>=| fsm.arcLabels.update(_ + (a -> label)))
    
  def props: Expression[List[Expression[EditableProperty]]] = selection.map(_.toList.flatMap({
    case p: State => List(StringProperty("Name", name(p)), BooleanProperty("Initial state", initial(p)), BooleanProperty("Final", terminal(p)))
    case a: Arc => List(StringProperty("Label", label(a)))
  }).toList)
} 