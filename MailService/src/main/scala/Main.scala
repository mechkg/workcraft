import javax.mail._
import javax.mail.internet._
import java.security.Security
import java.util.Properties
import java.io._
import org.workcraft.services._
import com.google.common.io.Files
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

object Copy {
	def apply(in: InputStream, out: OutputStream) = {
		val buffer = new Array[Byte](1024)
		var len = in.read(buffer)
		while (len != -1) {
			out.write(buffer, 0, len)
			len = in.read(buffer)
		}
	}
}

case class Sender (name: String, address: String)

case class Msg (sender: Sender, subject: String, body: String, attachments: File)

object MailReceiver {
	def plainTextFromAlternative (alt: Multipart): Option[String] = {
		var result:Option[String] = None
		Range(0, alt.getCount).foreach ( i => {
			val part = alt.getBodyPart(i)
			if (part.isMimeType ("text/plain")) result = Some(part.getContent.asInstanceOf[String])
		})
		result
	}

	def messageBody (msg: Message): Option[String] = {
		if (msg.isMimeType ("text/plain")) Some(msg.getContent.asInstanceOf[String])
		else if (msg.isMimeType ("multipart/alternative")) plainTextFromAlternative(msg.getContent.asInstanceOf[Multipart])
		else if (msg.isMimeType ("multipart/mixed")) {
			val body = msg.getContent.asInstanceOf[Multipart].getBodyPart(0)
			if (body.isMimeType ("text/plain")) Some(body.getContent.asInstanceOf[String])
			else if (body.isMimeType ("multipart/alternative")) plainTextFromAlternative(body.getContent.asInstanceOf[Multipart]) else None
		}
		else None
	}
	
	def attachments (msg: Message): List[Part] = {
		val list = scala.collection.mutable.ListBuffer[Part]()
		if (msg.isMimeType ("multipart/*")) {
			val multipart = msg.getContent.asInstanceOf[Multipart]
			Range(1, multipart.getCount).foreach ( i => {
				val part = multipart.getBodyPart(i)
				val disp = part.getDisposition
				if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT) && part.getFileName != null) {
					list += part
				}
			})
		}
		list.toList
	}
	                                    	
	def saveAttachments (parts: List[Part], dir: File) = {
	    parts.foreach (a => a.getFileName match {
		  case s: String => Copy (a.getInputStream, new FileOutputStream(new File(dir, s)))
		})
	}
	
	def receive (host: String, port: Int, user: String, pass: String): List[Msg] = 
	try
	{
		val props = System.getProperties()
		props.setProperty("mail.store.protocol", "imaps")
		props.setProperty("mail.imap.connectiontimeout", "5000")
		props.setProperty("mail.imap.timeout", "5000")

		val session = Session.getInstance(props)
		val store = session.getStore("imaps")

		println ("Fetching mail...")
		store.connect(host, port, user, pass)
		
		val folder = store.getDefaultFolder
		
		val inbox = folder.getFolder("inbox")

		val trash = folder.getFolder("[Gmail]/Trash")

		inbox.open(Folder.READ_WRITE)
		trash.open(Folder.READ_WRITE)

		val messages = inbox.getMessages()
		
		val result = messages.toList.map ( message => {
			val senderName = Option((message.getFrom()(0).asInstanceOf[InternetAddress]).getPersonal)
			val senderAddress = (message.getFrom()(0).asInstanceOf[InternetAddress]).getAddress
			val subject = Option(message.getSubject())
			val body = messageBody(message)
			val att = attachments(message)

			println("Message from: " + senderName.getOrElse("No name") + " <" + senderAddress +">")

			val tempDir = Files.createTempDir

            saveAttachments(att, tempDir)
			
			Msg(Sender(senderName.getOrElse("Mr.X"), senderAddress), subject.getOrElse(""), body.getOrElse(""), tempDir)
		})

		inbox.copyMessages (messages, trash)
		inbox.close(true)
		store.close()

		println (messages.length.toString + " messages received.")

		result
	}
}

object IOUtils {
  def partitionEither[A, B](list: List[Either[A, B]]) =
    list.foldRight((List[A](), List[B]()))((item, lists) => item match {
      case Left(left) => (left :: lists._1, lists._2)
      case Right(right) => (lists._1, right :: lists._2)
    })

  def open (file: File, globalServices: GlobalServiceProvider): IO[Either[String, List[ModelServiceProvider]]] = 
    globalServices.implementations(FileOpenService).map(_.open(file)).sequence >>= (_.flatten match {
       case Nil => ioPure.pure { Left ("No import plug-ins know how to read the file \"" + file.getName + "\".") }
       case x => x.map(_.job).sequence.map( results => {
         val (bad, good) = partitionEither(results)
         if (good.isEmpty) Left ("Could not open the file \"" + file.getName +"\" because:\n" + bad.map("-- " + _).mkString("\n"))
         else Right (good)
       })
      })

  def export (model: ModelServiceProvider, format: Format, file: File, globalServices: GlobalServiceProvider): IO[Option[String]] = {
    val exporters = globalServices.implementations(ExporterService).filter(_.targetFormat == Format.LolaPetriNet)
    val (unapplicable, applicable) = partitionEither(exporters.map(_.export(model)))

    if (applicable.isEmpty) {
     val explanation = if (exporters.isEmpty) " because no export plug-ins are available for this format."
                       else " because:\n" + unapplicable.map("-- " + _.toString).mkString ("\n") + "."
     ioPure.pure { Some ("Could not export the model as \"" + format.description + "\"" + explanation) }
    } else {
      applicable.head.job(file) >>=| ioPure.pure { None }
    }
   }

  def doUntilSuccessful (actions: List[IO[Option[String]]]): IO[(List[String], Boolean)] = {
    def rec (remaining: List[IO[Option[String]]], excuses: IO[List[String]]): IO[(List[String], Boolean)] =
      remaining match {
        case Nil => excuses.map ((_, false))
        case x :: xs => x >>= (res => if (res.isDefined) rec (xs, excuses.map( res.get :: _ )) else excuses.map((_, true)))
      }

    rec (actions, ioPure.pure { List[String]() } )
  }

  def convert (inputFile: File, targetFormat: Format, outputFile: File, globalServices: GlobalServiceProvider): IO[Either[String, File]] =
    if (inputFile.getName.endsWith(targetFormat.extension)) ioPure.pure { Files.copy(inputFile, outputFile); Right(outputFile) } // assume that the file is already in the correct format
    else {
      open (inputFile, globalServices) >>= {
        case Left(error) => ioPure.pure { Left(error) }
        case Right(models) => doUntilSuccessful(models.map(export(_, targetFormat, outputFile, globalServices))) >>= {
          case (_, true) => ioPure.pure { Right(outputFile) }
          case (excuses, false) => ioPure.pure { Left ("File format conversion was unsuccessful for the following reason(s):\n\n" + excuses.map ("-- " + _).mkString("\n\n")) }
        }
      }
    }
}

object MailSender {
  def send (address: String, subject: String, body: String) = {
    val props = new Properties
    props.setProperty("mail.transport.protocol","smtps")
    props.put("mail.smtp.auth","true")

    val session = Session.getInstance(props)
    val transport = session.getTransport()
    val addressFrom = new InternetAddress("verify@workcraft.org", "Workcraft Verification Service")

    val message = new MimeMessage(session)
    message.setSender(addressFrom)
    message.setFrom(addressFrom)
    message.setSubject(subject)
    message.setContent(body, "text/plain")
    
    val addressTo = new InternetAddress(address)
    message.setRecipient(Message.RecipientType.TO, addressTo)
    transport.connect("smtp.gmail.com", 465, "verify@workcraft.org", "ilikepetrinets")
    transport.sendMessage(message, Array(addressTo))
    transport.close()

    println ("Reply sent")
  }
}
  
object MailService extends App {
  import org.workcraft.plugins.lola._
  import org.workcraft.plugins.lola.LolaError._
  import org.workcraft.plugins.lola.LolaResult._
  import org.workcraft.tasks.TaskControl
                                                        
  val modules = List(new org.workcraft.plugins.petri2.PetriNetModule, new org.workcraft.plugins.lola.LolaModule, new org.workcraft.plugins.petrify.PetrifyModule)
  val globalServices = new GlobalServiceManager (modules)

  def timeOut (seconds: Double): IO[Boolean] = {
    val time = System.currentTimeMillis + (seconds * 1000.0).toInt

    ioPure.pure {
      if (System.currentTimeMillis > time) true else false
    }
  }

  def opening (sender: Sender) = "Hello " + sender.name.split(" ")(0) + ",\n\n"
  
  def closing = "\nTruthfully yours,\nWorkcraft."

  def lolaReport (timeout: Double, output:File, result: Either[Option[LolaError], LolaResult]): String = result match {
    case Left(None) => "Verification time limit set by my master " + "(%.2f seconds)".format(timeout) + " has been exceeded, so I had killed LoLA."
    case Left(Some(error)) => error match {
        case CouldNotStart(reason) => "I could not start LoLA because of this silly error: " + reason.toString
        case OutOfMemory => "LoLA ran out of memory trying to verify this model."
        case Syntax(reason) => "LoLA reported a syntax error in the input. I *could* have made an error writing the input file. The error was the following:\n" + reason
        case ArgsOrIO(reason) => "LoLA reported invalid arguments or IO error. This could have been caused by a file permissions issue or disk being full. The error was the following:\n" + reason
        case StateOverflow => "LoLA exceeded the state space limit. This net's state space is too large to be analysed by LoLA."
        case Undefined(reason) => "Unhandled exception was thrown in LoLA: " + reason
     }
    case Right(Positive(trace)) => { 
      //      val trace = scala.io.Source.fromFile(output).getLines.toList.tail.mkString(", ")
      "This net has a deadlock state. The event trace that leads into this state is as follows:\n" + trace + "."
    }
    case Right(Negative(_)) => {
      "This net is deadlock free."
    }
  }

  def verifyPetriNetDeadlockWithLola (timeout: Double, file: File): String = {
    println ("Verifying " + file.getName + " for deadlocks using LoLA")

    val lolaIn = File.createTempFile ("workcraft", ".lola")
    val lolaOut = File.createTempFile ("workcraft", ".lola.path")

    "--- " + file.getName + " ---\n" +
    (IOUtils.convert (file, Format.LolaPetriNet, lolaIn, globalServices) >>= {
      case Left(error) => ioPure.pure { error }
      case Right(file) => {
        val task = new LolaTask ("/home/mech/tools/lola-1.16/src/lola-deadlock", lolaIn, lolaOut)
        task.runTask (TaskControl (timeOut (timeout), _ => IO.Empty, _ => IO.Empty)) map (result => lolaReport (timeout, lolaOut, result))
       }
    }).unsafePerformIO + "\n"
  }

 while (true)  {
  try {
    MailReceiver.receive("imap.gmail.com", 993, "verify@workcraft.org", "ilikepetrinets").foreach {
      case Msg(sender, subject, body, attachments) =>  {
        val letter = new StringBuilder

        letter.append (opening(sender))
                
        if (body.toLowerCase.contains("petri net") && body.toLowerCase.contains ("deadlock")) {
         val files = attachments.listFiles
         if (files.isEmpty) letter.append ("You have asked me to check some Petri Nets for deadlocks, but you seem to have forgotten to attach the files.\n")
         else {
           letter.append ("You have asked me to check some Petri Nets for deadlocks. This is what I have got:\n\n")
           files.foreach(file => letter.append(verifyPetriNetDeadlockWithLola(3.0, file)))
         }
       } else {
         letter.append ("I could not understand your request. At the moment, I understand the words \"petri net\" and \"deadlock\". Please explain your request using those words. For example:\n\nMy Dear Workcraft, could you be so kind as to check the petri nets that I have sent you for deadlocks.\n\nThat is a joke. Just \"petri net deadlock\" will do. Although the former will also be accepted.\n")
       }

       letter.append (closing)

       MailSender.send (sender.address, "RE: " + subject, letter.toString)
    }
  }
  }
  catch {
  case e => e.printStackTrace()
  }
  Thread.sleep(10000)
  }
}
