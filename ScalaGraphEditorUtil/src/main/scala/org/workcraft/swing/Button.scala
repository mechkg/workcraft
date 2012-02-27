package org.workcraft.swing
import javax.swing.JButton
import Swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import scalaz._
import Scalaz._

class Button
        ( actionListener : SwingRef[ActionEvent => Swing[Unit]]
        , btn : JButton) extends Component {
  def setEnabled(enabled : Boolean) = unsafeToSwing(btn.setEnabled(enabled))
  def setActionListener(listener : ActionEvent => Swing[Unit]) = actionListener.write(listener)
}

object Button {
  def apply(btn : JButton) : Swing[Button] =
    newRef[ActionEvent => Swing[Unit]](_ => ().pure) >>=
    (listener => unsafeToSwing(btn.addActionListener(new ActionListener {
      override def actionPerformed(action : ActionEvent) = 
          (listener.read >>= (_(action))).unsafeRun.unsafePerformIO
    })) >| new Button(listener, btn))
  def make (name : String) : Swing[Button] = 
    unsafeToSwing(new JButton(name)) >>= (apply(_ : JButton))
}
