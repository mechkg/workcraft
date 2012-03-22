package org.workcraft.plugins.stg21

case class Store[T,A](extract : T, peek : T => A)

object Store {

}