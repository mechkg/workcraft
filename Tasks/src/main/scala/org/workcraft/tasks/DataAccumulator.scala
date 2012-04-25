package org.workcraft.tasks

import scala.collection.mutable.ListBuffer
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._
import java.util.Arrays

class DataAccumulator {
  val stderrData = ListBuffer[Array[Byte]]()
  val stdoutData = ListBuffer[Array[Byte]]()
  
  def stdout (data: Array[Byte]) = ioPure.pure { stdoutData += data } >| {}
  def stderr (data: Array[Byte]) = ioPure.pure { stderrData += data } >| {}
  
  def collectStdout = ioPure.pure {
    val length = stdoutData.foldLeft(0)(_+_.length)
    val result = new Array[Byte](length)
    stdoutData.foldLeft(0)( (offset, chunk) => { System.arraycopy(chunk, 0, result, offset, chunk.length); offset+chunk.length} )
    result
  }
  
  def collectStderr = ioPure.pure {
    val length = stderrData.foldLeft(0)(_+_.length)
    val result = new Array[Byte](length)
    stderrData.foldLeft(0)( (offset, chunk) => { System.arraycopy(chunk, 0, result, offset, chunk.length); offset+chunk.length} )
    result
  }  
}
