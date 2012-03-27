package org.workcraft.pluginmanager
import java.lang.reflect.Modifier
import java.util.UUID
import org.workcraft.logging.Logger
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._
import Logger._

class PluginManager(val version: UUID, val packages: Traversable[String], val manifestPath: String, val forceReconfigure: Boolean)(implicit logger: () => Logger[IO]) {

  private def logPluginErrors(issues: Traversable[PluginError]) = {
    issues.map({
      case PluginError.Abstract(name) => warning("Plugin class " + name + " is abstract")
      case PluginError.NoDefaultConstructor(name) => warning("Plugin class " + name + " does not have a public constructor with no arguments")
      case PluginError.Exception(name, e) =>
        warning("Plugin class " + name + " could not be loaded due to exception " +
          e.getClass().getSimpleName() + " ( " + e.getMessage() + ")")
    }).foldLeft({}.pure)(_ *> _)
  }

  private def logManifestReadError(error: ManifestReadError) = error match {
    case ManifestReadError.Empty() => warning("Manifest is empty")
    case ManifestReadError.VersionMismatch() => warning("Manifest is tagged with a different version")
    case ManifestReadError.Exception(e) =>
      warning("Manifest could not be read due to an exception: " + e.getClass().getSimpleName() +
        " (" + e.getMessage() + ")")
  }

  private def reconfigure: Traversable[Class[_]] = {
    unsafeInfo("Reconfiguring")

    val finder = new PluginFinder(packages)

    unsafeInfo("Searching for potential plugins in the classpath")

    val classesToProcess = finder.searchClassPath()

    unsafeInfo("Processing " + classesToProcess.size + " class file(s)")
    
    val results = PluginProcessor.processClasses(classesToProcess)

    unsafeInfo("Found " + results.plugins.size + " plugin(s)")

    val n = results.errors.size

    if (n > 0) {
      unsafeWarning("Encountered " + n + " errors")
      logPluginErrors(results.errors).unsafePerformIO
    }

    unsafeInfo("Writing manifest")

    PluginManifest.write(version, manifestPath, PluginProcessor.classNames(results.plugins)) match {
      case Some(e) => unsafeWarning("Could not write manifest due to an exception " +
        e.getClass().getSimpleName() + "(" + e.getMessage() + ")")
      case None => {}
    }

    unsafeInfo("Reconfiguration complete")

    results.plugins
  }

  private val classes: Traversable[Class[_]] = {
    if (forceReconfigure) {
      unsafeInfo("Reconfiguration requested by user")
      reconfigure
    } else {
      unsafeInfo("Reading plugin manifest")

      PluginManifest.read(version, manifestPath) match {
        case Right(manifest) => {
          unsafeInfo("Processing manifest")
          val results = PluginProcessor.processClasses(manifest)
          if (!results.errors.isEmpty) {
            unsafeWarning("Problems encountered while processing plugin classes listed in the manifest")
            logPluginErrors(results.errors).unsafePerformIO
            unsafeInfo("Will reconfigure")

            reconfigure
          } else {
            unsafeInfo("Successfully loaded plugin classes listed in the manifest")
            results.plugins
          }
        }
        case Left(error) => {
          logManifestReadError(error).unsafePerformIO
          unsafeInfo("Will reconfigure")

          reconfigure
        }
      }
    }
  }

  private val holders =
    classes.foldLeft(Map[Class[_], UntypedHolder]())((map, cls) => map + (cls -> new UntypedHolder(cls)))

  def plugins[T](cls: Class[T]): Traversable[PluginHolder[T]] =
    classes.filter(cls.isAssignableFrom(_)).map(c => new PluginHolder[T](holders(c)))
}