package org.workcraft.pluginmanager

class UntypedHolder (cls : Class[_]) {
  lazy val singleton = cls.newInstance()
  def newInstance = cls.newInstance()
}