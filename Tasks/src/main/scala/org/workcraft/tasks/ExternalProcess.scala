package org.workcraft.tasks

import java.io.File
import java.io.IOException
import java.util.LinkedList
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import java.util.concurrent.atomic.AtomicBoolean
import java.io.InputStream
import java.util.Arrays
import scalaz.Scalaz._

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

class ProcessWaiterThread(stdoutReader: Thread, stdinReader: Thread, process: ProcessHandle, doWhenFinished: (Int, Boolean) => IO[Unit]) extends Thread {
  override def run = {
    val exitValue = process.process.waitFor()
    stdoutReader.join()
    stdinReader.join()
    doWhenFinished(exitValue, process.cancelled).unsafePerformIO
  }
}

class CancelRequestMonitorThread(process: ProcessHandle, cancelRequest: IO[Boolean]) extends Thread {
  override def run: Unit = while (true)
    if (cancelRequest.unsafePerformIO) {
      process.cancel
      return
    } else
      Thread.sleep(10)
}

case class ProcessHandle private[tasks] (private[tasks] val process: Process) {
  private var cancelRequested = false
  def writeData(data: Array[Byte]): IO[Unit] = ioPure.pure { process.getOutputStream().write(data) }
  def cancel = { process.destroy(); cancelRequested = true }
  def cancelled = cancelRequested
}

trait ProcessListener {
  def stdout(data: Array[Byte]): IO[Unit]
  def stderr(data: Array[Byte]): IO[Unit]
  def finished(exitValue: Int, cancelled: Boolean): IO[Unit]
}

trait DiscardingListener extends ProcessListener {
  def stdout(data: Array[Byte]) = ioPure.pure {}
  def stderr(data: Array[Byte]) = ioPure.pure {}
}

trait SynchronousProcessListener {
  def stdout(data: Array[Byte]): IO[Unit]
  def stderr(data: Array[Byte]): IO[Unit]
}

class DiscardingSynchronousListener extends SynchronousProcessListener {
  def stdout(data: Array[Byte]): IO[Unit] = ioPure.pure {}
  def stderr(data: Array[Byte]): IO[Unit] = ioPure.pure {}
}

class AccumulatingSynchronousListener extends SynchronousProcessListener {
  val accumulator = new DataAccumulator

  def stdout(data: Array[Byte]) = accumulator.stdout(data)
  def stderr(data: Array[Byte]) = accumulator.stderr(data)
}

object ExternalProcess {
  def runAsync(command: List[String], workingDir: Option[File], listener: ProcessListener, cancelRequest: IO[Boolean]): IO[Either[Throwable, ProcessHandle]] = ioPure.pure {
    val processBuilder = new ProcessBuilder(scala.collection.JavaConversions.asJavaList(command))
    workingDir.foreach(processBuilder.directory(_))
    try {
      val process = processBuilder.start()

      val stdoutReader = new StreamReaderThread(process.getInputStream(), listener.stdout(_))
      stdoutReader.start()
      val stderrReader = new StreamReaderThread(process.getErrorStream(), listener.stderr(_))
      stderrReader.start()

      val handle = ProcessHandle(process)

      new CancelRequestMonitorThread(handle, cancelRequest)
      new ProcessWaiterThread(stdoutReader, stderrReader, handle, listener.finished(_, _)).start()

      Right(handle)
    } catch {
      case e: Throwable => Left(e)
    }
  }

  def runSynchronously(command: List[String], workingDir: Option[File], listener: SynchronousProcessListener, cancelRequest: IO[Boolean]): IO[Either[Throwable, (Int, Boolean)]] = ioPure.pure {
    val processBuilder = new ProcessBuilder(scala.collection.JavaConversions.asJavaList(command))
    workingDir.foreach(processBuilder.directory(_))
    try {
      val process = processBuilder.start()

      val stdoutReader = new StreamReaderThread(process.getInputStream(), listener.stdout(_))
      stdoutReader.start()
      val stderrReader = new StreamReaderThread(process.getErrorStream(), listener.stderr(_))
      stderrReader.start()

      val handle = ProcessHandle(process)

      new CancelRequestMonitorThread(handle, cancelRequest).start()

      val returnValue = process.waitFor()

      stdoutReader.join()
      stderrReader.join()

      Right((returnValue, handle.cancelled))
    } catch {
      case e: Throwable => Left(e)
    }
  }

  def runSyncDiscardOutput(command: List[String], workingDir: Option[File], cancelRequest: IO[Boolean]): IO[Either[Throwable, (Int, Boolean)]] =
    runSynchronously(command, workingDir, new DiscardingSynchronousListener, cancelRequest)

  def runSyncCollectOutput(command: List[String], workingDir: Option[File], cancelRequest: IO[Boolean]): IO[Either[Throwable, (Int, Boolean, Array[Byte], Array[Byte])]] =
    for {
      acc <- ioPure.pure { new AccumulatingSynchronousListener };
      res <- runSynchronously(command, workingDir, acc, cancelRequest);
      stderr <- acc.accumulator.collectStderr;
      stdout <- acc.accumulator.collectStdout
    } yield res.map(r => (r._1, r._2, stdout, stderr))
}
