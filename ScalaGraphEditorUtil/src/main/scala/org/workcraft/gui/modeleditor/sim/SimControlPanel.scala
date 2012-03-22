package org.workcraft.gui.modeleditor.sim
import javax.swing.JLabel
import javax.swing.BorderFactory
import javax.swing.border.LineBorder
import java.awt.Color
import javax.swing.SwingConstants
import javax.swing.JPanel
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Dimension
import javax.swing.BoxLayout
import java.awt.Graphics
import javax.swing.border.AbstractBorder
import java.awt.Component
import java.awt.Insets
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import java.awt.BorderLayout
import javax.swing.SwingUtilities
import javax.swing.JButton
import info.clearthought.layout.TableLayout
import info.clearthought.layout.TableLayoutConstants
import javax.swing.JScrollPane

case class Trace[Event, State](events: List[(Event, State)])

class CurvedBorder(color: Color, radius: Int) extends AbstractBorder {
  override def paintBorder(c: Component, g: Graphics, x: Int, y: Int,
    w: Int, h: Int) {
    g.setColor(color)
    g.drawRoundRect(x, y, w - 1, h - 1, radius, radius)
  }

  override def getBorderInsets(c: Component) = new Insets(radius, radius, radius, radius)

  override def getBorderInsets(c: Component, i: Insets) = {
    i.left = radius
    i.right = radius
    i.bottom = radius
    i.top = radius
    i
  }

  override val isBorderOpaque = true
}

class EventButton[Event, State](event: Event, state: State, toString: Event => String) extends JLabel(toString(event)) {
  this.setPreferredSize(new Dimension(100, 22))
  this.setHorizontalAlignment(SwingConstants.CENTER)
  this.setBorder(new CurvedBorder(Color.black, 20))
}

class TracePanel[Event, State](trace: Trace[Event, State], toString: Event => String) extends JPanel {
  this.setBackground(this.getBackground().brighter())
  this.setLayout(new GridBagLayout)

  val c = new GridBagConstraints
  c.weightx = 1
  c.weighty = 0
  c.gridx = 0
  c.fill = GridBagConstraints.HORIZONTAL
  c.insets = new Insets(1, 1, 1, 1)

  var i = 0

  trace.events.foreach { case (event, state) => { println(event); c.gridy = i; add(new EventButton(event, state, toString), c); i += 1 } }

  c.weighty = 1
  c.gridy = i

  add(new JPanel, c)
}

class SimControlPanel[Event, State](t: Expression[Trace[Event, State]], toString: Event => String) extends JPanel {
  val sz = Array(
      Array (30, 0.5, 0.5, 30),
      Array (20, TableLayoutConstants.FILL)
      )
  
  setLayout(new TableLayout(sz))

  val refresh = swingAutoRefresh(t, (trace: Trace[Event, State]) => ioPure.pure {
    
    val kojo = new JScrollPane
    
    removeAll()
    
    val l = new JButton("\u25c4")
    l.setFocusable(false)
    val r = new JButton("\u25ba")
    r.setFocusable(false)
    add (l, "0 0 C C")
    add(new TracePanel[Event, State](trace, toString), "1 0 1 1")
    //add(new TracePanel[Event, State](trace, toString), "2 0 2 1")
    add (r, "3 0 C C")
    
    
    revalidate()
  })
}