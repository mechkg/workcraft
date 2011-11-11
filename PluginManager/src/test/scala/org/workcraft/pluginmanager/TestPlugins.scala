import org.workcraft.pluginmanager.Plugin

package org.workcraft.pluginmanager {
  class NotAPlugin
  class GoodPlugin extends Plugin
  abstract class AbstractPlugin extends Plugin
  class PluginWithNoDefaultConstructor(val x: Int) extends Plugin
}

package somepackage {
  class PluginInAWrongPackage extends Plugin
}