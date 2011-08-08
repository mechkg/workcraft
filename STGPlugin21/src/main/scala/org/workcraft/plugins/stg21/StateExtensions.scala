package org.workcraft.plugins.stg21

object StateExtensions extends scalaz.States {
  import scalaz.State
  import fields._
  implicit def decorateState[S,V](s : State[S,V]) = {
    class Q {
      def on[WS](f : Field[WS,S]) : State[WS, V] = {
        scalaz.Scalaz.state[WS,V](ws => {
          val (s1, v) = s(f.get(ws))
          (f.set(s1)(ws), v)
        }
        )
      }
    }
    new Q
  }
 
}