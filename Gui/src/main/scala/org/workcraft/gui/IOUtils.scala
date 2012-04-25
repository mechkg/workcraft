package org.workcraft.gui

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import org.workcraft.services.ExportError
import org.workcraft.services.ExporterService
import org.workcraft.services.FileOpenService
import org.workcraft.services.Format
import scalaz.Scalaz._
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.GlobalServiceProvider
import java.io.File
import com.google.common.io.Files

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

  def guessFormatFromExtension (file: File): Option[Format] = file.getName.lastIndexOf('.') match {
    case -1 => None
    case i => {
      val ext = file.getName.substring (i)
      println (ext)
      Format.knownFormats.find (_.extension == ext)
    }
  }
  
  def export (model: ModelServiceProvider, format: Format, file: File, globalServices: GlobalServiceProvider): Either[String, IO[Option[ExportError]]] = {
    val exporters = globalServices.implementations(ExporterService).filter(_.targetFormat == format)
    val (unapplicable, applicable) = partitionEither(exporters.map(_.export(model)))

    if (applicable.isEmpty) {
     val explanation = if (exporters.isEmpty) " because no export plug-ins are available for this format."
                       else " because:\n" + unapplicable.map("-- " + _.toString).mkString ("\n") + "."
     Left ("Cannot export the model as \"" + format.description + "\"" + explanation)
    } else {
      Right(applicable.head.job(file))
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
/*
  def convert (inputFile: File, targetFormat: Format, outputFile: File, globalServices: GlobalServiceProvider): IO[Option[String]] =
    if (inputFile.getName.endsWith(targetFormat.extension)) ioPure.pure { Files.copy(inputFile, outputFile); Right(outputFile) } // assume that the file is already in the correct format
    else {
      open (inputFile, globalServices) >>= {
        case Left(error) => ioPure.pure { Some(error) }
        case Right(models) => doUntilSuccessful(models.map(export(_, targetFormat, outputFile, globalServices))) >>= {
          case (_, true) => ioPure.pure { Right(outputFile) }
          case (excuses, false) => ioPure.pure { Left ("File format conversion was unsuccessful for the following reason(s):\n\n" + excuses.map ("-- " + _).mkString("\n\n")) }
        }
      }
    }*/
}
