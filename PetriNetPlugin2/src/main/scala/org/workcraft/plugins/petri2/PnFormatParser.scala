package org.workcraft.plugins.petri2
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.lexical.Lexical
import scala.util.parsing.combinator.lexical.Scanners
import scala.util.parsing.combinator.token.Tokens
import scala.util.parsing.combinator.syntactical.TokenParsers
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

sealed trait PnTokens

object PnTokens {
  sealed trait Token

  sealed trait Keyword extends Token

  case object Places extends Keyword
  case object Layout extends Keyword
  case object Transitions extends Keyword
  case object Arcs extends Keyword
  case object Marking extends Keyword

  case object LeftBracket extends Token
  case object Eof extends Token
  case object RightBracket extends Token

  case object Comma extends Token
  case class Name(name: String) extends Token
  case class Number(s: String) extends Token
  case class ErrorToken(msg: String) extends Token
}

class PnLexer extends Scanners with RegexParsers {
  override type Elem = Char
  override type Token = PnTokens.Token

  import PnTokens._

  def places = "Places:".r ^^^ Places
  def transitions = "Transitions:".r ^^^ Transitions
  def arcs = "Arcs:".r ^^^ Arcs
  def layout = "Layout:".r ^^^ Layout
  def marking = "Marking:".r ^^^ Marking
  def name = PetriNet.namePattern.r.map(Name(_))

  def number = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?".r.map(Number(_))

  def lb = "\\(".r.map(_ => LeftBracket)
  def rb = "\\)".r.map(_ => RightBracket)
  def comma = ",".r.map(_ => Comma)
  def eof = "\\z".r.map(_ => Eof)

  def token = places | transitions | arcs | layout | marking | number | name | lb | rb | comma | eof

  def whitespace = "\\s*".r

  def errorToken(msg: String) = ErrorToken(msg)

  def scanner(in: String) = new Scanner(in)

  def scanner(in: File) = new Scanner(new PagedSeqReader(PagedSeq.fromSource(scala.io.Source.fromFile(in)(scala.io.Codec.UTF8))))
}

class PnFormatParser extends Parsers {
  val lexer = new PnLexer

  case class PartialResult(places: List[String], transitions: List[String], arcs: List[(String, String)], layout: List[(String, (Double, Double))], marking: List[(String, Int)]) {
    def +(other: PartialResult) =
      PartialResult(places ::: other.places, transitions ::: other.transitions, arcs ::: other.arcs, layout ::: other.layout, marking ::: other.marking)
  }

  override type Elem = PnTokens.Token

  import PnTokens._

  def isInt(s: String) = s.matches("[0-9]+")

  def name = log (acceptMatch("valid place/transition name", { case Name(s) => s })) ("name")

  def double = log (acceptMatch("double-precision coordinate", { case Number(v) => v.toDouble }))("double")

  def integer = log ( acceptMatch("non-negative integer", { case Number(v) if isInt(v) => v.toInt; })) ("integer")

  def arc = log ( (LeftBracket ~> (name ~ name) <~ RightBracket) map (r => (r._1, r._2))) ("arc")

  def position = log ((LeftBracket ~> (name ~ (double ~ double)) <~ RightBracket) map (r => (r._1, (r._2._1, r._2._2)))) ("position")

  def marking = log ((LeftBracket ~> (name ~ integer) <~ RightBracket) map (r => (r._1, r._2))) ("marking")

  def section: Parser[PartialResult] = (acceptIf(e => e match {
    case _: Keyword => true
    case _ => false
  })(e => "expected 'Places:', 'Transitions:', 'Arcs:', 'Marking:' or 'Layout:' but found " + e.toString())).flatMap({
    case Places => (name*) map (ps => PartialResult(ps, Nil, Nil, Nil, Nil))
    case Transitions => (name*) map (ts => PartialResult(Nil, ts, Nil, Nil, Nil))
    case Arcs => (arc*) map (arcs => PartialResult(Nil, Nil, arcs, Nil, Nil))
    case Layout => (position*) map (pos => PartialResult(Nil, Nil, Nil, pos, Nil))
    case Marking => (marking*) map (mrk => PartialResult(Nil, Nil, Nil, Nil, mrk))
  }).flatMap(p => (section | Eof ^^^ (PartialResult(Nil, Nil, Nil, Nil, Nil))) map (p + _))

  def parse(in: File): IO[Either[String, VisualPetriNet]] = ioPure.pure {
    section(lexer.scanner(in)) match {
      case Success(a, b) => {
        val allNames = a.places ++ a.transitions
        val distinctNames = allNames.distinct

        if (allNames.length != distinctNames.length)
          Left("Duplicate names found: " + (allNames -- distinctNames).mkString(", "))
        else {
          val places = a.places.map(name => (new Place, name))
          val placeLabelling = places.toMap
          val placeLookup = places.map(_.swap).toMap

          val transitions = a.transitions.map(name => (new Transition, name))
          val transitionLabelling = transitions.toMap
          val transitionLookup = transitions.map(_.swap).toMap

          def componentLookup(name: String) = List(placeLookup.get(name), transitionLookup.get(name)).flatten match {
            case Nil => None
            case a :: Nil => Some(a)
          }

          val arcs = a.arcs.map(a => a match {
            case (from, to) =>
              if (placeLookup.contains(from) && transitionLookup.contains(to))
                Right(new ConsumerArc(placeLookup(from), transitionLookup(to)))
              else if (placeLookup.contains(to) && transitionLookup.contains(from))
                Right(new ProducerArc(transitionLookup(from), placeLookup(to)))
              else
                Left(a)
          })

          val arcErrors = arcs.flatMap({ case Right(_) => None; case Left(e) => Some(e.toString()) })

          if (!arcErrors.isEmpty)
            Left("Following arcs refer to undefined names:\n" + arcErrors.mkString(" "))
          else {
            val goodArcs = arcs.flatMap({ case Right(a) => Some(a); case Left(_) => None })

            val layout = a.layout.foldRight(Map[Component, Point2D.Double]())((entry, map) => {
              val (name, (x, y)) = entry
              componentLookup(name) match {
                case Some(c) => map + (c -> new Point2D.Double(x, y))
                case None => map
              }
            }).withDefaultValue(new Point2D.Double(0, 0))

            val marking = a.marking.foldRight(Map[Place, Int]())((entry, map) => {
              val (name, tokens) = entry
              placeLookup.get(name) match {
                case Some(c) => map + (c -> tokens)
                case None => map
              }
            }).withDefaultValue(0)

            Right(VisualPetriNet(PetriNet(marking, placeLabelling ++ transitionLabelling, places.map(_._1), transitions.map(_._1), goodArcs), layout, Map().withDefaultValue(Polyline(List()))))
          }
        }
      }
      case err => Left(err.toString)
    }
  }
}
