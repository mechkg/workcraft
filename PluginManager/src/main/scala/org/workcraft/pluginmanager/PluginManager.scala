package org.workcraft.pluginmanager
import java.lang.reflect.Modifier
import java.util.UUID
import org.workcraft.logging.Logger

class PluginManager(val version: UUID, val packages: Traversable[String], val manifestPath: String, val logger: Logger) {

  private def logPluginErrors(issues: Traversable[PluginError]) = {
    issues.foreach(issue => issue match {
      case PluginError.Abstract(name) => logger.warning("Plugin class " + name + " is abstract")
      case PluginError.NoDefaultConstructor(name) => logger.warning("Plugin class " + name + " does not have a public constructor with no arguments")
      case PluginError.Exception(name, e) =>
        logger.warning("Plugin class " + name + " could not be loaded due to exception " +
          e.getClass().getSimpleName() + " ( " + e.getMessage() + ")")
    })
  }

  private def logManifestReadError(error: ManifestReadError) = error match {
    case ManifestReadError.Empty() => logger.warning("Manifest is empty")
    case ManifestReadError.VersionMismatch() => logger.warning("Manifest is tagged with a different version")
    case ManifestReadError.Exception(e) =>
      logger.warning("Manifest could not be read due to an exception: " + e.getClass().getSimpleName() +
        " (" + e.getMessage() + ")")
  }

  private def reconfigure: Traversable[Class[_]] = {
    logger.info("Reconfiguring")

    val finder = new PluginFinder(packages)

    logger.info("Searching for potential plugins in the classpath")

    val classesToProcess = finder.searchClassPath()

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