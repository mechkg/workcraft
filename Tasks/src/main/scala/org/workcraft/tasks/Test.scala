package org.workcraft.tasks

import scalaz.Scalaz
import scalaz.Scalaz._
import Task.TaskMA

object Test {
  def main(args: Array[String]) = {
    class Task1(input: Int) extends Task [Double, String] {
      def runTask = Right(input * 10)
    }
    
    class Task2(input: Double) extends Task[String, String] {
      def runTask = Right(input.toString())
    }
    
    val z = for {
      x <- new Task1(8); 
      y <- new Task2(x)
    } yield x+8
    
    //val q = TaskMA(z)
    
    val o = z >>= (x => Task.pure("хуй"))
    
    //q.replicateM_(8)
    
    println (z.runTask)
  }
} 