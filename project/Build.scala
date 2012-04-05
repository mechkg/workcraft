import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Workcraft extends Build {
  val repos = Seq("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository", DefaultMavenRepository, "Workcraft Maven Repository" at "http://workcraft.org/maven2")

  lazy val workcraft = Project(id = "workcraft", base = file("Workcraft")) 
  .settings (assemblySettings:_*)
  .aggregate (gui, pnplugin, lolaplugin, petrifyplugin, fsmplugin, dotplugin)
  .dependsOn (gui, pnplugin, lolaplugin, petrifyplugin, fsmplugin, dotplugin)

  lazy val util = Project(id = "util", base = file("Util"))

  lazy val depMan = Project (id = "depman", base = file ("DependencyManager")) 
  .settings (libraryDependencies := Seq("com.google.code.pcollections" % "pcollections" % "1.0.0"), resolvers := repos) 
  .dependsOn (util)

  lazy val scalautil = Project(id = "scalautil", base = file("ScalaUtil"))
  .settings (libraryDependencies := Seq("org.scalaz" %% "scalaz-core" % "6.0.3"))
  .dependsOn (util, depMan)

  lazy val tasks = Project(id = "tasks", base = file ("Tasks"))
  .dependsOn (scalautil)

  lazy val logger = Project(id = "logger", base = file ("Logger"))
  .dependsOn (scalautil)

  lazy val pluginManager = Project(id = "pluginmanager", base = file ("PluginManager"))
  .dependsOn (logger)

  lazy val core = Project(id = "core", base = file ("Core"))
  .dependsOn (pluginManager, tasks)

  lazy val graphics = Project(id = "graphics", base = file("Graphics"))
  .dependsOn (scalautil, booleanFormulae)

  lazy val booleanFormulae = Project (id = "booleanformulae", base = file ("BooleanFormulae"))
  .dependsOn (scalautil)
  .settings (libraryDependencies := Seq ("junit" % "junit" % "4.8.2"))


  lazy val gui = Project(id = "gui", base = file ("Gui"))
  .settings (libraryDependencies := Seq ( "org.streum" %% "configrity" % "0.9.0", "org.apache.xmlgraphics" % "batik-svg-dom" % "1.7", "org.apache.xmlgraphics" % "batik-svggen" % "1.7",
                                          "org.apache.xmlgraphics" % "batik-bridge" % "1.7", "tablelayout" % "TableLayout" % "20050920", "org.flexdock" % "flexdock" % "1.1.1",
                                          "commons-logging" % "commons-logging" % "1.1", "org.pushingpixels" % "substance" % "6.1")
             , resolvers := repos)
  .dependsOn (core, graphics)

  lazy val pnplugin = Project (id = "pnplugin", base = file ("PetriNetPlugin2"))
  .dependsOn (gui, graphedutil)

  lazy val graphedutil = Project (id = "graphedutil", base = file ("ScalaGraphEditorUtil"))
  .dependsOn (scalautil, graphics, gui)

  lazy val lolaplugin = Project(id = "lolaplugin", base = file ("LolaPlugin"))
  .dependsOn (pnplugin)

  lazy val petrifyplugin = Project (id = "petrifyplugin", base = file ("PetrifyPlugin2"))
  .dependsOn (pnplugin, fsmplugin, gui)

  lazy val dotplugin = Project (id = "dotplugin", base = file ("DotPlugin"))
  .dependsOn (gui)

  lazy val fsmplugin = 	Project (id = "fsmplugin", base = file ("FSMPlugin"))
  .dependsOn (gui, pnplugin, graphedutil)

  lazy val mailservice = Project (id = "mailservice", base = file ("MailService"))
  .settings (assemblySettings:_*)
  .settings (libraryDependencies := Seq ("javax.mail" % "mail" % "1.4.5", "com.google.guava" % "guava" % "11.0.2"))
  .dependsOn(core, pnplugin, lolaplugin)
}
