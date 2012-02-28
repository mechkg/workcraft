package org.workcraft.pluginmanager

class PluginHolder [T] (val untyped: UntypedHolder) {
  lazy val singleton = untyped.singleton.asInstanceOf[T]
  def newInstance = untyped.newInstance.asInstanceOf[T]
}