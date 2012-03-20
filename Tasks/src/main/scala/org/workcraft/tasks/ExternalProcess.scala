package org.workcraft.tasks

import java.io.File
import java.io.IOException
import java.util.LinkedList
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import java.util.concurrent.atomic.AtomicBoolean
import java.io.InputStream
import java.util.Arrays

class StreamReaderThread(stream: InputStream, read: Array[Byte] => IO[Unit]) extends Thread {
  val buffer = new Array[Byte](0x10000)

  override def run: Unit =
    while (true)
      try {
        val result = stream.read(buffer)

        if (result == -1)
          return

        if (result > 0)
          read(Arrays.copyOfRange(buffer, 0, result)).unsafePerformIO

        Thread.sleep(1)
      } catch {
        case e: Throwable => return
        //  printStackTrace(); -- This exception is mostly caused by the process termination and spams the user with information about exceptions that should
        //  just be ignored, so removed printing. mech. 
      }
}

class ProcessWaiterThread (process: Process, doWhenFinished: Int => IO[Unit]) extends Thread {
  override def run = {
    doWhenFinished(process.waitFor()).unsafePerformIO
  }
}

case class ProcessHandle private[tasks] (private[tasks] val process: Process) {
  def writeData (data: Array[Byte]): IO[Unit] = ioPure.pure { process.getOutputStream().write(data) }
  def cancel: IO[Unit] = ioPure.pure { process.destroy() }
}

trait ProcessListener {
  def stdout (data: Array[Byte]): IO[Unit]
  def stderr (data: Array[Byte]): IO[Unit]
  def finished (exitValue: Int) : IO[Unit]
}

trait DiscardingListener extends ProcessListener {
  def stdout (data: Array[Byte]) = ioPure.pure {}
  def stderr (data: Array[Byte]) = ioPure.pure {}
}

object ExternalProcess {
  def run(command: List[String], workingDir: Option[File], listener: ProcessListener): IO[Either[Throwable, ProcessHandle]] = ioPure.pure {
    val processBuilder = new ProcessBuilder(scala.collection.JavaConversions.asJavaList(command))
    workingDir.foreach(processBuilder.directory(_))
    try {
      val process = processBuilder.start()
      
      new StreamReaderThread (process.getInputStream(), listener.stdout(_)).start()
      new StreamReaderThread (process.getErrorStream(), listener.stderr(_)).start()
      new ProcessWaiterThread (process, listener.finished(_)).start()
      
      Right(ProcessHandle(process))
    } catch {
      case e: Throwable => Left(e)
    }
  }
}