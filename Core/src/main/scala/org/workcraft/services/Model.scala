package org.workcraft
package object services {
  type Model = ModelServiceProvider
  type ModelService[T] = Service[ModelScope, T]
  type GlobalService[T] = Service[GlobalScope, T]
}
