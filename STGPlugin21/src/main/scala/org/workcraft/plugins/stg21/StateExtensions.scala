package org.workcraft.plugins.stg21
import scalaz.Lens

object StateExtensions extends scalaz.States {
  import scalaz.State
  import fields._
  implicit def decorateState[S,V](s : State[S,V]) = {
    class Q {
      def on[WS](f : Lens[WS,S]) : State[WS, V] = {
        scalaz.Scalaz.state[WS,V](ws => {
          val (s1, v) = s(f.get(ws))
          (f.set(ws,s1), v)
        }
        )
      }
    }
    new Q
  }
 
}