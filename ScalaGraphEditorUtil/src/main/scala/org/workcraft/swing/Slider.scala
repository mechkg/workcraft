package org.workcraft.swing

import javax.swing.JSlider
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import Swing._
import scalaz._
import Scalaz._

class Slider
     ( slider : JSlider
     , changeListener : SwingRef[ChangeEvent => Swing[Unit]]) extends Component {
  def value = unsafeToSwing(slider.getValue)
  def setChangeListener(listener : ChangeEvent => Swing[Unit]) = changeListener.write(listener)
}
object Slider {
  def apply(sld : JSlider) : Swing[Slider] =
    newRef[ChangeEvent => Swing[Unit]](_ => ().pure) >>=
    (listener => unsafeToSwing(sld.addChangeListener(new ChangeListener {
      override def stateChanged(change : ChangeEvent) = 
           (listener.read >>= (_(change))).unsafeRun.unsafePerformIO
    })) >| new Slider(sld, listener))
  
  def make(a : Int, b : Int, c : Int) : Swing[Slider] = unsafeToSwing(new JSlider(a, b, c)) >>= (apply(_))
}
