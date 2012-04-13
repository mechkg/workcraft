package org.workcraft.services

trait Module {
  def name: String
  def description: String = name
  def serviceProvider: GlobalServiceProvider
}
