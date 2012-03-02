package org.workcraft.gui
import org.streum.configrity.Configuration
import org.workcraft.logging.Logger
import org.workcraft.scala.effects.IO
import org.workcraft.logging.Logger._
import javax.swing.UIManager
import java.io.File

object ConfigKeys {
  val xSize = "xSize"
  val ySize = "ySize"
  val xPos = "xPos"
  val yPos = "yPos"
  val maximised = "maximised"
  val lookandfeel = "lookandfeel"
}

case class GuiConfiguration(val xPos: Int,
  val yPos: Int, val xSize: Int, val ySize: Int, maximised: Boolean, lookandfeel: String)

object GuiConfiguration {
  def load(path: String)(implicit logger: () => Logger[IO]): Option[GuiConfiguration] = {
    unsafeInfo("Loading GUI configuration")

    try {
      val config = Configuration.load(path)
      Some(GuiConfiguration(
        config[Int](ConfigKeys.xPos, 0),
        config[Int](ConfigKeys.yPos, 0),
        config[Int](ConfigKeys.xSize, 0),
        config[Int](ConfigKeys.ySize, 0),
        config[Boolean](ConfigKeys.maximised, false),
        config[String](ConfigKeys.lookandfeel, UIManager.getCrossPlatformLookAndFeelClassName())))

    } catch {
      case e: Throwable => { unsafeWarning("Failed to load GUI configuration"); warning(e).unsafePerformIO; None }
    }
  }

  def save(path: String, conf: GuiConfiguration)(implicit logger: () => Logger[IO]) = {
    unsafeInfo("Saving GUI configuration")

    val guiConfig = Configuration().set(ConfigKeys.xSize, conf.xSize)
      .set(ConfigKeys.ySize, conf.ySize)
      .set(ConfigKeys.maximised, conf.maximised)
      .set(ConfigKeys.xPos, conf.xPos)
      .set(ConfigKeys.yPos, conf.yPos)
      .set(ConfigKeys.lookandfeel, conf.lookandfeel)
    try {
      guiConfig.save(path)
    } catch {
      case e: Throwable => { unsafeWarning("Could not save GUI configuration"); warning(e).unsafePerformIO }
    }
  }
}