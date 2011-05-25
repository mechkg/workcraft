package org.workcraft.plugins.cpog.scala

object Scalaz extends scalaz.MAs {
	def pure[F[_] : scalaz.Pure] = scalaz.Scalaz.pure
}
