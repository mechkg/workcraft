package org.workcraft.tasks

import scalaz.Scalaz
import scalaz.Scalaz._
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import Task.taskMA

object Test {
  
  def flood : Task[Unit, Nothing] = Task({
    System.out.println("piska")
    Thread.sleep(100)
    Right(())
  }.pure[IO]) flatMap (x => flood)
  
  def flood2 : Task[Unit, Nothing] = Task({
    System.out.println("siska")
    Thread.sleep(100)
    Right(())
  }.pure[IO]) flatMap (x => flood2)
  
  
  def main(args: Array[String]) = {
    
    
/*    val z = for {
      x <- Task( { Right(8) }.pure[IO]); 
      y <- Task( { Right(x.toString())}.pure[IO])
    } yield y */
    
    var cancelled = false
    
    println("launching!")
    
    flood.runAsynchronously(cancelled.pure).unsafePerformIO
    flood2.runAsynchronously(cancelled.pure).unsafePerformIO
    

    println("launched!")

    readLine
    
    cancelled = true
    
    //val q = Task( { Left(8) }.pure[IO]) flatMap ( x => Task( { Left(x.toString())}.pure[IO]))
    //println (z.runTask())
  }
}