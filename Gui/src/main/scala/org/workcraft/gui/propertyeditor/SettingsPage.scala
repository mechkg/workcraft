package org.workcraft.gui.propertyeditor

case class Config

trait SettingsPage extends Properties {
  def save(config:Config):Unit
  def load(config:Config):Unit
  def getSection():String
  def getName():String
}
