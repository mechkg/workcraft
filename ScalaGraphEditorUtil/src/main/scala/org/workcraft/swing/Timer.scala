package org.workcraft.swing

import javax.swing.{Timer => JTimer}
import Swing._

class Timer(btn : JTimer) extends Component {
  def stop : Swing[Unit] = unsafeToSwing(btn.stop)
  def setDelay(delay : Int) = unsafeToSwing(btn.setDelay(delay)) 
  def setInitialDelay(delay : Int) = unsafeToSwing(btn.setInitialDelay(delay)) 
  def start = unsafeToSwing(btn.start)
}

