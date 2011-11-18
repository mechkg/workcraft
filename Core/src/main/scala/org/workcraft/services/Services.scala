package org.workcraft.services

sealed trait Scope

trait GlobalScope extends Scope

trait ModelScope extends Scope

trait Service[S <: Scope, ImplT]

trait ServiceProvider[S <: Scope] {
  def implementation[T] (service: Service[S, T]) : Option[T]
}

trait GlobalServiceProvider extends ServiceProvider[GlobalScope]

trait ModelServiceProvider extends ServiceProvider[ModelScope]