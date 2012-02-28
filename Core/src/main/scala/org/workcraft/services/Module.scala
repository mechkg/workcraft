package org.workcraft.services

import org.workcraft.pluginmanager.Plugin

trait Module extends Plugin {
  def name: String
  def description: String = name
  def serviceProvider: GlobalServiceProvider
}
