package org.workcraft.services

import org.workcraft.pluginmanager.Plugin

trait Module extends Plugin {
  def name : String
  def description : String = ""
  def services : Traversable[GlobalServiceProvider]
}
