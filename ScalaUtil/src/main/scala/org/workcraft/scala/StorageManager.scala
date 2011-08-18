package org.workcraft.scala
import org.workcraft.dependencymanager.advanced.user.{StorageManager => JStorageManager}
import org.workcraft.scala.Expressions._

class StorageManager(val storageManager : JStorageManager) {
  def create[T](initialValue : T) : ModifiableExpression[T] = storageManager.create(initialValue)
}
