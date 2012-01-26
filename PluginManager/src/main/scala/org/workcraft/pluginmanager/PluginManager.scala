package org.workcraft.pluginmanager
import java.lang.reflect.Modifier
import java.util.UUID
import org.workcraft.logging.Logger._
import org.workcraft.logging.Logger
import scalaz.effects.IO
import scalaz.effects.IO._
import scalaz.Scalaz.ma
import scalaz.Scalaz._
import scalaz.Traverse._
import scalaz.Traverse

class PluginManager(val version: UUID, val packages: List[String], val manifestPath: String)(implicit logger:Logger[IO]) {

  private def logPluginErrors(issues: List[PluginError]) = {
    issues.map({
      case PluginError.Abstract(name) => warning("Plugin class " + name + " is abstract")
      case PluginError.NoDefaultConstructor(name) => warning("Plugin class " + name + " does not have a public constructor with no arguments")
      case PluginError.Exception(name, e) =>
        logger.warning("Plugin class " + name + " could not be loaded due to exception " +
          e.getClass().getSimpleName() + " ( " + e.getMessage() + ")")
    }).foldLeft ( {}.pure ) (_*>_)
  }

  private def logManifestReadError(error: ManifestReadError) = {error match {
    case ManifestReadError.Empty() => warning("Manifest is empty")
    case ManifestReadError.VersionMismatch() => warning("Manifest is tagged with a different version")
    case ManifestReadError.Exception(e) =>
      logger.warning("Manifest could not be read due to an exception: " + e.getClass().getSimpleName() +
        " (" + e.getMessage() + ")")
  }}.pure

  private def reconfigure: IO[Traversable[Class[_]]] = {
    val finder = new PluginFinder(packages)
    
    info("Reconfiguring") *>
    info("Searching for potential plugins in the classpath") *>
    
    classesToProcess = finder.searchClassPath()

    logger.info("Processing " + classesToProcess.size + " class files")

    val results = PluginProcessor.processClasses(classesToProcess)

    logger.info("Found " + results.plugins.size + " plugins")

    val n = results.errors.size

    if (n > 0) {
      logger.warning ("Encountered " + n + " errors")
      logPluginErrors(results.errors)
    }

    logger.info ("Writing manifest")

    PluginManifest.write(version, manifestPath, PluginProcessor.classNames(results.plugins)) match {
      case Some(e) => logger.warning ("Could not write manifest due to an exception " +
        e.getClass().getSimpleName() + "(" + e.getMessage() + ")")
      case None => {}
    }

    logger.info("Reconfiguration complete")

    results.plugins
  }

  private val classes: Traversable[Class[_]] = {
    logger.info("Reading plugin manifest")

    PluginManifest.read(version, manifestPath) match {
      case Right(manifest) => {
        logger.info("Processing manifest")
        val results = PluginProcessor.processClasses(manifest)
        if (!results.errors.isEmpty) {
          logger.warning("Problems encountered while processing plugin classes listed in the manifest")
          logPluginErrors(results.errors)
          logger.info("Will reconfigure")

          reconfigure
        } else {
          logger.info("Successfuly loaded plugin classes listed in the manifest")
          results.plugins
        }
      }
      case Left(error) => {
        logManifestReadError(error)
        logger.info("Will reconfigure")

        reconfigure
      }
    }
  }
  
  private val holders = 
    classes.foldLeft(Map[Class[_], UntypedHolder]())( (map, cls) => map + (cls -> new UntypedHolder(cls)))
  
  def plugins[T] (cls: Class[T]) : Traversable[PluginHolder[T]] =
    classes.filter(cls.isAssignableFrom(_)).map (c => new PluginHolder[T](holders(c)))
}