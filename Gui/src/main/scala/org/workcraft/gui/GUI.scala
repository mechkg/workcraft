package org.workcraft.gui
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import javax.swing.ImageIcon
import java.awt.Color
import org.apache.batik.util.XMLResourceDescriptor
import org.apache.batik.dom.svg.SAXSVGDocumentFactory
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.GVTBuilder
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Window
import javax.swing.KeyStroke
import javax.swing.JMenuItem
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.JButton
import java.io.IOException
import javax.imageio.ImageIO
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.Viewport
import java.awt.Rectangle
import javax.swing.Icon
import java.awt.Insets
import java.awt.Dimension

object GUI {
  def loadImageFromResource(path: String): Either[Throwable, BufferedImage] = this.getClass().getClassLoader().getResource(path) match {
    case null => Left(new IOException("Resource not found: " + path))
    case url => try { Right(ImageIO.read(url)) } catch { case e => Left(e) }
  }

  def menuItem(text: String, mnemonic: Option[Char], accelerator: Option[KeyStroke], action: IO[Unit]) = {
    val result = new JMenuItem(text)
    mnemonic.foreach(result.setMnemonic(_))
    accelerator.foreach(result.setAccelerator(_))
    result.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) = {
        action.unsafePerformIO
      }
    })
    result
  }

  def button(text: String, action: () => Unit) = {
    val result = new JButton(text)
    result.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) = {
        action()
      }
    })
    result
  }

  def createIconFromImage(resourcePath: String): IO[Option[ImageIcon]] = ioPure.pure {
    val res = this.getClass().getClassLoader().getResource(resourcePath)
    if (res == null)
      None
    else
      Some(new ImageIcon(res))
  }

  def createIconFromSvgUsingSettingsSize(path: String) =
    CommonVisualSettings.settings.map(_.iconSize).eval >>= (size => createIconFromSvg(path, size, size, None))

  def createIconFromSvg(path: String, height: Int, width: Int, background: Option[Color]): IO[ImageIcon] = ioPure.pure {
    System.setProperty("org.apache.batik.warn_destination", "false")

    val parser = XMLResourceDescriptor.getXMLParserClassName()
    val f = new SAXSVGDocumentFactory(parser)

    val document = f.createDocument(this.getClass().getClassLoader().getResource(path).toString())

    val userAgentAdapter = new UserAgentAdapter()
    val bridgeContext = new BridgeContext(userAgentAdapter)
    val builder = new GVTBuilder()

    val graphicsNode = builder.build(bridgeContext, document)

    val sizeY = bridgeContext.getDocumentSize().getHeight()
    val sizeX = bridgeContext.getDocumentSize().getWidth()

    val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    val g2d = bufferedImage.getGraphics.asInstanceOf[Graphics2D]

    background.foreach(bg => {
      g2d.setColor(bg)
      g2d.fillRect(0, 0, width, height)
    })

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    val scaleX = (width - 1) / sizeX
    val scaleY = (height - 1) / sizeY
    val scale = Math.min(scaleX, scaleY)
    g2d.scale(scale, scale)
    g2d.translate(0.5, 0.5)

    graphicsNode.paint(g2d)
    g2d.dispose()
    new ImageIcon(bufferedImage)
  }

  def createIconButton(icon: Icon, toolTip: String) = for {
    size <- CommonVisualSettings.settings.map(_.iconSize).eval
  } yield {
    val result = new JButton(icon)
    result.setToolTipText(toolTip)
    result.setMargin(new Insets(0, 0, 0, 0))
    val insets = result.getInsets()
    val minSize = size + Math.max(insets.left + insets.right, insets.top + insets.bottom)
    result.setPreferredSize(new Dimension(minSize, minSize))
    result
  }

  def centerToParent(frame: Window, parent: Window) = {
    val parentSize = parent.getSize
    val mySize = frame.getSize
    val q = parent.getLocationOnScreen
    frame.setLocation(((parentSize.width - mySize.width) / 2) + q.x, ((parentSize.height - mySize.height) / 2) + q.y)
  }

  def centerAndSizeToParent(frame: Window, parent: Window) = {
    val parentSize = parent.getSize
    frame.setSize(parentSize.width / 2, parentSize.height / 2)
    centerToParent(frame, parent)
  }

  def editorMessage(viewport: Viewport, color: Color, message: String): Expression[GraphicalContent] = for {
    font <- CommonVisualSettings.settings.map(_.editorMessageFont);
    shape <- viewport.dimensions
  } yield GraphicalContent(g => {
    g.setFont(font)
    val r = g.getFont().getStringBounds(message, g.getFontRenderContext()).getBounds()

    val (x, y, w, h) = shape

    r.x = w / 2 - r.width / 2
    r.y = h - 20 - r.height

    g.setColor(new Color(240, 240, 240))
    g.fillRoundRect(r.x - 10, r.y - 10, r.width + 20, r.height + 20, 5, 5)
    g.setColor(new Color(224, 224, 224))
    g.drawRoundRect(r.x - 10, r.y - 10, r.width + 20, r.height + 20, 5, 5)
    g.setColor(color)
    val lm = g.getFont().getLineMetrics(message, g.getFontRenderContext())
    g.drawString(message, r.x, r.y + r.height - lm.getDescent().toInt)
  })
}
