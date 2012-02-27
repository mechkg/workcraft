package org.workcraft.plugins.petri.tools;

trait SimControl[M[_],Event] {
  def canFire(event : Event) : M[Boolean]
  def fire(event : Event) : M[Unit]
  def getNextEvent : M[Event]
  def unfire : M[Unit]
}
