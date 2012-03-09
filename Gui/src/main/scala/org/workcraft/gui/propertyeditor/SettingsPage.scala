package org.workcraft.gui.propertyeditor

import org.workcraft.Config


trait SettingsPage extends Properties {
  def save(config:Config):Unit
  def load(config:Config):Unit
  def getSection():String
  def getName():String
}
