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

object DotGParser extends Parsers with RegexParsers {
  override type Elem = Char

  sealed trait Direction

  object Direction {
    case object Plus extends Direction
    case object Minus extends Direction
    case object Toggle extends Direction
  }

  sealed trait GraphElement

  object GraphElement {
    case class SignalTransition(name: String, direction: Direction, instance: Int) extends GraphElement
    case class PlaceOrDummy(name: String) extends GraphElement
  }

  sealed trait PlaceRef

  object PlaceRef {
    case class ImplicitPlace(from: GraphElement, to: GraphElement) extends PlaceRef
    case class ExplicitPlace(name: String) extends PlaceRef
  }

  case class PartialDotG(model: Option[String] = None, inputs: List[String] = Nil, outputs: List[String] = Nil, dummy: List[String] = Nil,
    internal: List[String] = Nil, graph: Map[GraphElement, List[GraphElement]] = Map(), marking: Map[PlaceRef, Int] = Map()) {
    def +(other: PartialDotG) =
      PartialDotG(
        if (model.isDefined) model else other.model,
        inputs ++ other.inputs,
        outputs ++ other.outputs,
        dummy ++ other.dummy,
        internal ++ other.internal,
        graph ++ other.graph,
        marking ++ other.marking)
  }

/*  class PetriNetBuilder(parseResult: PartialDotG) {
    import Direction._
    import GraphElement._
    import PlaceRef._

    def isDummy(name: String) = parseResult.dummy.contains(name)

    def stName(t: SignalTransition) = t.name + "_" + (t.direction match {
      case Plus => "plus"
      case Minus => "minus"
      case Toggle => "toggle"
    }) + (if (t.instance == 0) "" else "_" + t.instance)
    
    def getOrCreateTransition (name: String, net: PetriNet) = net.names.get(name) match {
      case Some(t:Transition) => (t, net)
      case None => (new Transition, net.copy (transitions = net.transitions + t))
    }

    def buildPetriNet: PetriNet = parseResult.graph.foldLeft(PetriNet.Empty) {
      case (net, (elem, postset)) => {
        
        val src = elem match {
          case t:SignalTransition => stName(t)
          case PlaceOrDummy(name) => if (isDummy(name)) Some(name) else None          
        }
        
                
        
      }
    }
  }*/

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

  def line = (graphElement ~ (graphElement*)) <~ (emptyline*)
  
  def graph = (".graph" ~ (emptyline+)) ~> (line+) ^^ (s => PartialDotG(graph = s.foldLeft(Map[GraphElement, List[GraphElement]]())((map, line) => map + line)))

  def model = (".model" ~> """.+""".r <~ (emptyline+)) ^^ (s => { println ("model"); PartialDotG(model = Some(s)) })
  def dummy = (".dummy" ~> (name+) <~ (emptyline+)) ^^ (s => PartialDotG(dummy = s))
  def inputs = (".inputs" ~> (name+) <~ (emptyline+)) ^^ (s => PartialDotG(inputs = s))
  def outputs = (".outputs" ~> (name+) <~ (emptyline+)) ^^ (s => PartialDotG(outputs = s))
  def internal = (".internal" ~> (name+) <~ (emptyline+)) ^^ (s => PartialDotG(internal = s))

  def m = (placeRef ~ (("=" ~> number)?)) ^^ { case a ~ b => (a -> b.getOrElse(0)) }

  def implicitPlaceRef = "<" ~> (graphElement <~ ",") ~ graphElement <~ ">" ^^ { case from ~ to => ImplicitPlace(from, to) }

  def placeRef = implicitPlaceRef | (name ^^ (ExplicitPlace(_)))

  def marking = (".marking" ~> "{" ~> (m+) <~ "}" <~ (emptyline+)) ^^ (s => PartialDotG(marking = s.toMap))

  def stg = ((model | dummy | inputs | outputs | internal | graph | marking)+) <~ ".end" ^^ (_.foldLeft(PartialDotG())(_ + _))
  
  def stgFile = phrase ((emptyline*) ~> stg <~ (emptyline*))

  def parseDotG(file: File) = parse(stgFile, (new BufferedReader(new FileReader(file)))) match {
    case Success(r, _) => Right(r)
    case err => Left(err.toString)
  }
}

object Test extends App {
  
  println ("""(?m)$""".matches("\n"))
  println (DotGParser.parseDotG(new File("e:/winpetrify/stgshka.g")))
}