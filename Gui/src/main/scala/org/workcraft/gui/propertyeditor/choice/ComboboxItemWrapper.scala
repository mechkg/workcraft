package org.workcraft.gui.propertyeditor.choice

import org.workcraft.util.Pair


class ComboboxItemWrapper {
  /*
  def this(pair:Pair[String]) = {
    this(pair.getFirst(), pair.getSecond())
  }

  def this(text:String, value:Object) = {
    this.value = value
    this.text = text
  }
  */
  def getValue():Object = {
    return value
  }

  override  def toString():String = {
    return text
  }
  private val value:Object = null
  private val text:String = null
}
