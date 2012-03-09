package org.workcraft.gui.propertyeditor

import java.awt.Component


trait RendererProvider[T ] {
  def createRenderer(value:T):Component
}
