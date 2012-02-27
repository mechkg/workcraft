package org.workcraft.swing

trait HasRefs[M[_]] {
  type Ref[_]
  def newRef[A](x : A) : M[Ref[A]]
  def readRef[A](ref : Ref[A]) : M[A]
  def writeRef[A](ref : Ref[A])(a : A) : M[Unit]
  
  implicit def decorateRef[A] (ref : Ref[A]) = new {
    def read = readRef(ref)
    def write = writeRef(ref)(_)
  }
}
