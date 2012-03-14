package org.workcraft.plugins.petri2
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.lexical.Lexical
import scala.util.parsing.combinator.lexical.Scanners
import scala.util.parsing.combinator.token.Tokens
import scala.util.parsing.combinator.syntactical.TokenParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader

sealed trait PnTokens

object PnTokens {
  sealed trait Token

  case object LeftBracket extends Token
  case object Eof extends Token
  case object RightBracket extends Token
  
  sealed trait Keyword extends Token
  
  case object Places extends Keyword
  case object Layout extends Keyword
  case object Transitions extends Keyword
  case object Arcs extends Keyword
    
  case object Comma extends Token
  case class Name(name: String) extends Token
  case class IntNumber(value: Int) extends Token
  case class DoubleNumber(value: Double) extends Token  
  case class ErrorToken(msg:String) extends Token
}

class PnLexer extends Scanners with RegexParsers {
  override type Elem = Char
  override type Token = PnTokens.Token
  
  import PnTokens._

  def places = "Places".r ^^^ Places
  def transitions = "Transitions".r ^^^ Transitions
  def arcs = "Arcs".r ^^^ Arcs
  def layout = "Layout".r ^^^ Layout
  def name = PetriNetSnapshot.namePattern.r.map(Name(_))
  def integer = "[0-9]+".r.map(s => IntNumber(s.toInt))
  def double = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?".r.map(s => DoubleNumber(s.toDouble))
  
  def lb = "\\(".r.map(_ => LeftBracket)
  def rb = "\\)".r.map(_ => RightBracket)
  def comma = ",".r.map(_ => Comma)
  def eof = "\\z".r.map (_ => Eof)

  def token = places | transitions | arcs | layout | double | name | integer | lb | rb | comma | eof
    
  def whitespace = "\\s*".r
  
  def errorToken(msg: String) = ErrorToken(msg)
  
  def scanner(in: String) = new Scanner(in)
}

class PnFormatParser extends Parsers {
  case class PartialResult (places: List[String], transitions: List[String], arcs: List[(String, String)], layout: List[(String, (Double, Double))]) {
    def + (other: PartialResult) = PartialResult (places ::: other.places, transitions ::: other.transitions, arcs ::: other.arcs, layout ::: other.layout)
  }
  
  override type Elem = PnTokens.Token 
  
  import PnTokens._
  
  def name = acceptMatch("valid place/transition name", { case Name(s) => s })
  
  def double = acceptMatch("double-precision coordinate", {case DoubleNumber(v) => v})
  
  def arc = (LeftBracket ~> (name ~ name) <~ RightBracket) map ( r => (r._1, r._2))
  
  def position = (LeftBracket ~> (name ~ (double ~ double)) <~ RightBracket) map ( r => (r._1, (r._2._1, r._2._2)) )
      
  def section: Parser[PartialResult] = acceptIf(e => e match {case _: Keyword => true
      case _ => false
    })(e => "expected 'Places', 'Transitions', 'Arcs' or 'Layout' but found " + e.toString()) flatMap ({
      case Places => (name+) map (ps => PartialResult(ps, Nil, Nil, Nil)) 
      case Transitions => (name+) map (ts => PartialResult(Nil, ts, Nil, Nil))
      case Arcs => (arc+) map (arcs => PartialResult(Nil, Nil, arcs, Nil))
      case Layout => (position+) ^^^ (PartialResult(List("gavno"), List("gavno"), Nil, Nil))
    }) flatMap ( p => (section map (p+_)) | Eof ^^^ (PartialResult(Nil, Nil, Nil, Nil)))
    
  def net = section
}

object Test extends PnFormatParser with App {
  val l = new PnLexer
  
  net(l.scanner("Places p0 p1 p2 p3 Transitions t0 t1 t2 t3 Arcs (p0 t1) (p1 t3) Layout (p0 1 1)")) match {
    case Success(a, b) => print(a)
    case x => println(x)
  }
}