package org.workcraft.pluginmanager
import java.lang.reflect.Modifier

sealed trait PluginError {
  def className: String
}

object PluginError {
  case class Abstract(val className: String) extends PluginError
  case class NoDefaultConstructor(val className: String) extends PluginError
  case class Exception(val className: String, val e: Throwable) extends PluginError
}

class PluginResults(val results: Traversable[Either[PluginError, Class[_]]]) {
  lazy val errors = results.flatMap(
    x => x match {
      case Left(issue) => Some(issue)
      case _ => None
    })

  lazy val plugins = results.flatMap(
    x => x match {
      case Right(plugin) => Some[Class[_]](plugin)
      case _ => None
    })
}

object PluginResults {
  implicit def apply(results: Traversable[Either[PluginError, Class[_]]]) = new PluginResults(results)
}

object PluginProcessor {
  def processClass(className: String): Option[Either[PluginError, Class[_]]] = {
    try {
      val cls = Class.forName(className)

      if (classOf[Plugin].isAssignableFrom(cls) && !cls.equals(classOf[Plugin])) {
        if (Modifier.isAbstract(cls.getModifiers()))
          Some(Left(PluginError.Abstract(className)))
        else {
          val defCons = cls.getConstructor()
          Some(Right(cls))
        }
      } else
        None
    } catch {
      case e: NoSuchMethodException => Some(Left(PluginError.NoDefaultConstructor(className)))
      case e => Some(Left(PluginError.Exception(className, e)))
    }
  }

  def processClasses(classNames: Traversable[String]): PluginResults = classNames.flatMap(processClass(_))
  
  def classNames (classes: Traversable[Class[_]]) = classes.map (_.getName())
}