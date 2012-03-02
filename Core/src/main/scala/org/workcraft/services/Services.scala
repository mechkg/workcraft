package org.workcraft.services

sealed trait Scope

trait GlobalScope extends Scope

trait ModelScope extends Scope

trait Service[S <: Scope, ImplT]

trait ServiceProvider[S <: Scope] {
  def implementation[T](service: Service[S, T]): Option[T]
}

trait GlobalServiceProvider extends ServiceProvider[GlobalScope]

object GlobalServiceProvider {
  val Empty = new GlobalServiceProvider {
    def implementation[T](service: Service[GlobalScope, T]) = None
  }
}

trait ModelServiceProvider extends ServiceProvider[ModelScope]

object ModelServiceProvider {
  val Empty = new ModelServiceProvider {
    def implementation[T](service: Service[ModelScope, T]) = None
  }
}