package org.workcraft.gui.propertyeditor.choice

import org.workcraft.util.Pair


class ComboboxItemWrapper(pair : (String, Any)) {

  def getValue:Any = {
    return value
  }

  override  def toString():String = {
    return text
  }
  private def value:Any = pair._2
  private val text:String = pair._1
}
