package org.workcraft.plugins.petri.tools

trait SimStateControl[M[_]] {
  def reset : M[Unit]
  def rememberInitialState : M[Unit]
}
