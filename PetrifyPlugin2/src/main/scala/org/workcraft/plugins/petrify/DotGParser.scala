package org.workcraft.plugins.petrify

import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader
import java.io.InputStream
import scala.collection.immutable.PagedSeq
import scala.util.parsing.input.PagedSeqReader
import java.awt.geom.Point2D
import java.io.File
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import org.workcraft.dom.visual.connections.Polyline
import java.io.BufferedReader
import java.io.FileReader
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.plugins.petri2.PetriNet._
import scalaz._
import Scalaz._
import org.workcraft.plugins.petri2.Transition
import scala.util.matching.Regex
import org.workcraft.tasks.Task
import org.workcraft.tasks.TaskControl

object DotGParser extends Parsers with RegexParsers {
  override type Elem = Char

  sealed trait Direction

  object Direction {
    case object Plus extends Direction { override def toString = "+" }
    case object Minus extends Direction { override def toString = "-" }
    case object Toggle extends Direction { override def toString = "~" }
  }

  sealed trait GraphElement

  object GraphElement {
    case class SignalTransition(name: String, direction: Direction, instance: Int) extends GraphElement { override def toString = name + direction + (if (instance > 0) "/" + instance else "") }
    case class PlaceOrDummy(name: String) extends GraphElement { override def toString = name }
  }

  sealed trait PlaceRef

  object PlaceRef {
    case class ImplicitPlace(from: GraphElement, to: GraphElement) extends PlaceRef { override def toString = "<" + from + ", " + to + ">" }
    case class ExplicitPlace(name: String) extends PlaceRef { override def toString = name }
  }

  case class StateRef(name: String) { override def toString = name }

  case class DotG(model: Option[String] = None, inputs: List[String] = Nil, outputs: List[String] = Nil, dummy: List[String] = Nil,
    internal: List[String] = Nil, graph: Map[GraphElement, List[GraphElement]] = Map(), stateGraph: List[(StateRef, GraphElement, StateRef)] = List(), marking: Map[PlaceRef, Int] = Map()) {
    def +(other: DotG) =
      DotG(
        if (model.isDefined) model else other.model,
        inputs ++ other.inputs,
        outputs ++ other.outputs,
        dummy ++ other.dummy,
        internal ++ other.internal,
        graph ++ other.graph,
        stateGraph ++ other.stateGraph,
        marking ++ other.marking)
  }

  import Direction._
  import GraphElement._
  import PlaceRef._

  implicit def tuple[A, B](t: ~[A, B]) = (t._1, t._2)

  override val whiteSpace = """[ \t]+""".r

  def dir = "[\\+\\-\\~]".r ^^ { case "+" => Direction.Plus; case "-" => Direction.Minus; case "~" => Direction.Toggle }
  def instance = "/" ~> number
  def signalTransition = name ~ dir ~ (instance?) ^^ { case name ~ dir ~ inst => SignalTransition(name, dir, inst.getOrElse(0)) }

  def name = """[a-zA-Z_][0-9a-zA-Z_]+""".r
  def number = "[0-9]+".r ^^ (_.toInt)

  def emptyline = """(?m)(#.*)?\r?\n""".r

  def graphElement: Parser[GraphElement] = signalTransition | (name ^^ (PlaceOrDummy(_)))

  def line = (graphElement ~ (graphElement*)) <~ (emptyline+)

  def graph = (".graph" ~ (emptyline+)) ~> (line+) ^^ (s => DotG(graph = s.foldLeft(Map[GraphElement, List[GraphElement]]())((map, line) => map + line)))

  def stategraphline = (name ~ graphElement ~ name) <~ (emptyline+) ^^ { case s1 ~ ge ~ s2 => ((StateRef(s1), ge, StateRef(s2))) }

  def stategraph = (".state graph" ~ (emptyline+)) ~> (stategraphline+) ^^ (s => DotG(stateGraph = s))

  def model = (".model" ~> """.+""".r <~ (emptyline+)) ^^ (s => DotG(model = Some(s)))
  def dummy = (".dummy" ~> (name+) <~ (emptyline+)) ^^ (s => DotG(dummy = s))
  def inputs = (".inputs" ~> (name+) <~ (emptyline+)) ^^ (s => DotG(inputs = s))
  def outputs = (".outputs" ~> (name+) <~ (emptyline+)) ^^ (s => DotG(outputs = s))
  def internal = (".internal" ~> (name+) <~ (emptyline+)) ^^ (s => DotG(internal = s))

  def m = log((placeRef ~ (("=" ~> number)?)) ^^ { case a ~ b => (a -> b.getOrElse(1)) })("placeRef")

  def implicitPlaceRef = log( "<" ~> (graphElement <~ ",") ~ graphElement <~ ">" ^^ { case from ~ to => ImplicitPlace(from, to) })("implicitPlaceRef")

  def placeRef = implicitPlaceRef | (name ^^ (ExplicitPlace(_)))

  def marking = (".marking" ~> "{" ~> (m+) <~ "}" <~ (emptyline+)) ^^ (s => DotG(marking = s.toMap))

  def stg = ((model | dummy | inputs | outputs | internal | graph | stategraph | marking)+) <~ ".end" ^^ (_.foldLeft(DotG())(_ + _))

  def stgFile = phrase((emptyline*) ~> stg <~ (emptyline*))

  def parseDotG(file: File): IO[Either[String, DotG]] = ioPure.pure {
    parse(stgFile, (new BufferedReader(new FileReader(file)))) match {
      case Success(r, _) => Right(r)
      case err => Left(err.toString)
    }
  }

  def parseTask(file: File) = new Task[DotG, String] {
    def runTask(tc: TaskControl) =
      (tc.descriptionUpdate("Reading " + file.getPath) >>=| (parseDotG(file))).map {
        case Left(err) => Left(Some(err))
        case Right(dotg) => Right(dotg)
      }
  }
}

object Test extends App {
  (DotGParser.parseDotG(new File("e:/winpetrify/stgshka.g")) >>= {
    case Left(err) => ioPure.pure { println(err) }
    case Right(dotg) => PetriNetBuilder.buildPetriNet(dotg).map(println(_))
  }).unsafePerformIO
}
