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
import java.awt.BorderLayout
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
import java.awt.BasicStroke
import org.workcraft.scala.effects.IO
import java.awt.event.MouseAdapter
import javax.swing.UIManager
import java.awt.event.MouseEvent

class CurvedBorder(color: Color, radius: Int, thickness: Int) extends AbstractBorder {
  override def paintBorder(c: Component, g: Graphics, x: Int, y: Int,
    w: Int, h: Int) {
    
    g.setColor(color)
    g.asInstanceOf[Graphics2D].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.asInstanceOf[Graphics2D].setStroke(new BasicStroke(thickness))
            
    g.drawRoundRect(x+ thickness/2, y + thickness/2, w - thickness * 2, h - thickness * 2, radius -thickness, radius -thickness)
    
  }

  override def getBorderInsets(c: Component) = new Insets(radius, radius, radius, radius)

  override def getBorderInsets(c: Component, i: Insets) = {
    i.left = radius + thickness
    i.right = radius + thickness
    i.bottom = radius + thickness
    i.top = radius + thickness
    i
  }

  override val isBorderOpaque = true
}

class EventButton[Event, State](label: String, selected: Boolean, goto: IO[Unit]) extends JLabel(label) {
  
  val border = if (selected) 2 else 1 
  val borderRadius = 20
  
  var mouseIn = false
  val borderColor = Color.black
  
  val bgColor1 = UIManager.getColor("Label.background")
  val bgColor2 = UIManager.getColor("Label.background").brighter()

  this.setPreferredSize(new Dimension(120, 30))
  this.setHorizontalAlignment(SwingConstants.CENTER)
  this.setVerticalAlignment(SwingConstants.CENTER)
  this.setToolTipText("Click to jump to this state")
  this.setFocusable(false)
  
  override def paint (g: Graphics) {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    
    val w = this.getWidth
    val h = this.getHeight
    
    g2d.setColor(if (mouseIn || selected) bgColor2 else bgColor1)
    g2d.fillRoundRect(border/2, border/2, w - border * 2, h - border*2, borderRadius, borderRadius)
    
    g2d.setColor(borderColor)
    g2d.setStroke (new BasicStroke(border))
    g2d.drawRoundRect(border/2, border/2, w - border * 2, h - border*2, borderRadius, borderRadius)
    
    super.paint(g)
  } 
  
  
  this.addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent) = { mouseIn = true; repaint() }
    override def mouseExited(e: MouseEvent) = { mouseIn = false; repaint() }
    override def mouseClicked (e: MouseEvent) = { if (e.getButton() == 1) goto.unsafePerformIO }
  })
}

class TracePanel[Event, State](trace: MarkedTrace[Event, State], name: Event => String, gotoState: Int => IO[Unit]) extends JPanel {
  this.setBorder(BorderFactory.createLineBorder(Color.black))
  this.setBackground(this.getBackground().brighter())
  this.setLayout(null)
  
  setFocusable(false)
  
  val q = new EventButton("initial state", 0 == trace.position, gotoState(0))
  q.setBounds(5, 5, 120, 30)
  add(q)

  trace.trace.events.indices.foreach { i =>
    trace.trace.events(i) match {
      case (event, state) => {
        val q = new EventButton(name(event), i == trace.position-1, gotoState(i+1))
        q.setBounds(5, 5 + (i+1) * 35, 120, 30)
        add(q)
      }
    }
  }

  setPreferredSize(new Dimension(100, trace.trace.events.length * 25))
}

class SimControlPanel[Event, State](t: Expression[MarkedTrace[Event, State]], toString: Event => String, goto: Int => IO[Unit]) extends JPanel {
  val sz = Array(
    Array(30, 0.5, 0.5, 30),
    Array(20, TableLayoutConstants.FILL))

//  setLayout(new TableLayout(sz))
  setLayout (new BorderLayout())
  setFocusable(false)

  val refresh = swingAutoRefresh(t, (trace: MarkedTrace[Event, State]) => ioPure.pure {
    val kojo = new JScrollPane
    kojo.setFocusable(false)

    removeAll()

/*    val l = new JButton("\u25c4")
    l.setFocusable(false)
    val r = new JButton("\u25ba")
    r.setFocusable(false)
    add(l, "0 0 C C")*/
    add(new TracePanel[Event, State](trace, toString, goto), BorderLayout.CENTER)//"0 0 3 1")
    //add(new TracePanel[Event, State](trace, toString), "2 0 2 1")
//    add(r, "3 0 C C")

    revalidate()
  })
}
