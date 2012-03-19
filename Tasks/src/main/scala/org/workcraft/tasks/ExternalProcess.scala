package org.workcraft.tasks

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.util.LinkedList

import org.workcraft.scala.effects.IO

class ExternalProcess {
  
class StreamReaderThread (channel: ReadableByteChannel)  extends Thread
	{
		val buffer = ByteBuffer.allocate(1024)
		
		def handleData(data: Array[Byte])

		def run: Unit =
			while (true)
				try {
					buffer.rewind()
					val result = channel.read(buffer)

					if (result == -1) 
						return
					
					if (result > 0) {
						

					buffer.rewind()
					val data = new Array[Byte](result)
					buffer.get(data)

					handleData(data)
					}
					
					Thread.sleep(1)
				} catch {
				  case e: Throwable => return
                    //  printStackTrace(); -- This exception is mostly caused by the process termination and spams the user with information about exceptions that should
					//  just be ignored, so removed printing. mech. 
				}
		
	}  
		
	object InputReaderThread extends StreamReaderThread(inputStream) {
		
		InputReaderThread()
		{
			super(inputStream);
		}
		
		void handleData(byte[] data) {
			outputData(data);
		}
	}
	
	class ErrorReaderThread extends StreamReaderThread {
		ErrorReaderThread()
		{
			super(errorStream);
		}

		void handleData(byte[] data) {
			errorData(data);
		}
	}

	class WaiterThread extends Thread {
		public void run() {
			try {
				process.waitFor();
				processFinished();
			} catch (InterruptedException e) {
			}			
		}
	}
	
	private Process process = null;
	private boolean finished = false;

	private ReadableByteChannel inputStream = null;
	private ReadableByteChannel errorStream = null;
	private WritableByteChannel outputStream = null;

	private LinkedList<ExternalProcessListener> listeners = new LinkedList<ExternalProcessListener>();

	public ExternalProcess (String[] command, String workingDirectory) {
		processBuilder = new ProcessBuilder(command);
		processBuilder.directory(workingDirectory == null? null : new File(workingDirectory));
	}

	public ExternalProcess(String[] array, File workingDir) {
		this(array, workingDir.getAbsolutePath());
	}

	private void outputData(byte[] data) {
		for (ExternalProcessListener l : listeners)
			l.outputData(data);
	}

	private void errorData(byte[] data) {
		for (ExternalProcessListener l : listeners)
			l.errorData(data);
	}

	private void processFinished() {
		for (ExternalProcessListener l : listeners){
			l.processFinished(process.exitValue());
		}
		finished = true;
	}

	public boolean isRunning() {
		return process != null && !finished;		
	}

	public void start() throws IOException {

	}

	public void cancel() {
		if (isRunning())
			process.destroy();
	}

	public void writeData(byte[] data) {
		try {
			outputStream.write(ByteBuffer.wrap(data));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void addListener(ExternalProcessListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ExternalProcessListener listener) {
		listeners.remove(listener);
	}

	public void closeInput() throws IOException {
		outputStream.close();
	}

}

case class ProcessHandle private[tasks] {
//  def = fd
}

object ExternalProcess {
  def start (command: List[String], workingDir : Option[File], stdout: Array[Byte] => IO[Unit], stdin: Array[Byte] => IO[Unit]): IO[ProcessHandle] = {
		val processBuilder = new ProcessBuilder(scala.collection.JavaConversions.asJavaList(command))
		workingDir.foreach(processBuilder.directory(_))
		val process = processBuilder.start()
		
		val outputStream = Channels.newChannel(process.getOutputStream)
		val errorStream = Channels.newChannel(process.getErrorStream)
		val inputStream = Channels.newChannel(process.getInputStream)
		
		/*new InputReaderThread().start()
		new ErrorReaderThread().start()
		new WaiterThread().start()    */
  }
}