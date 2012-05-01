package org.workcraft.util
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import scala.util.matching.Regex
import java.awt.geom.Point2D
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader
import java.io.InputStream
import scala.collection.immutable.PagedSeq
import scala.util.parsing.input.PagedSeqReader


/* # rerew rwe
 * # rwe rwe
 * # erwer wer ew 
 * version { }
 *
 * initial state { s0 }
 * 
 * final states { s0 s1 s2 }
 * 
 * graph {
 *   s0 -> s1
 *   s1 -> s2
 *   s3 -> s4
 * }
 * 
 * layout {
 *   s0 14 15
 *   s1 14 51
 *   s5 53 15
 * }
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * */

case class WorkFile (sections: Map[String, String])

case class Layout (layout: Map[String, Point2D.Double])

case class Vertex (label: String)

case class Graph (v: Set[Vertex], e: Set[(Vertex, Vertex)]) {
  def overlay (other : Graph) = Graph (v ++ other.v, e ++ other.e)
  def sequence (other: Graph) = Graph (v ++ other.v, e ++ other.e ++ (for ( v1 <- v; v2 <- other.v) yield (v1, v2))) 
}

object Graph {
  val Empty = Graph (Set(), Set())
}

object WorkUtil extends RegexParsers {
  object GraphParser extends Parsers with RegexParsers {
    override type Elem = Char
    
    sealed trait Term
    
    case class NumLit(v: Int) extends Term
    case class Expr(terms: List[Term]) extends Term
    
    def vertex = WorkUtil.name ^^ ( s => Graph(Set(Vertex(s)), Set()))
    
    def term: Parser[Graph] = ("(" ~> expr <~ ")" | vertex) ~ (("->" ~> term)*) ^^ { case g1 ~ g2 => g2.foldLeft(g1)(_.sequence(_))}
    
    def expr: Parser[Graph] = (term+) ^^ (_.foldLeft(Graph.Empty)(_.overlay(_)))
    
    def parse (input: String): Either[String, Graph] = parse(phrase(expr), input) match {
      case Success (g, _) => Right(g)
      case err => Left (err.toString)
    }
  }

  def name = """[a-zA-Z0-9_]+""".r

  def section = regexMatch ("""(?s)\s*([a-zA-Z0-9_ ]+)\s*\{(.*?)\}""".r) ^^ ( m => {
    val g = Regex.Groups.unapplySeq(m).get
    (g(0).replaceAll("(?s)\\s*$", ""), g(1))
  })

  def regexMatch(r: Regex): Parser[Regex.Match] = new Parser[Regex.Match] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      (r findPrefixMatchOf (source.subSequence(start, source.length))) match {
        case Some(matched) =>
          Success(matched,
                  in.drop(start + matched.end - offset))
        case None =>
          Failure("string matching regex `"+r+"' expected but `"+in.first+"' found", in.drop(start - offset))
      }
    }
  }

  def work = ((section)*) ^^ (_.toMap)

  def parseWorkFile (file: File): Either[String, WorkFile] = 
    parse(work, (new BufferedReader(new FileReader(file)))) match {
      case Success(r, _) => Right(WorkFile(r))
      case err => Left(err.toString)
    }

  def parseGraphSection (f: WorkFile): Either[String, Graph] = {
    if (f.sections.contains("graph")) GraphParser.parse(f.sections("graph"))
    else Left ("This file has no graph section")
  }
}

object Test extends App {
  WorkUtil.parseWorkFile (new File("/home/mech/test.work")) match {
    case Left(err) => println(err) 
    case Right(work) => WorkUtil.parseGraphSection(work) match {
      case Left(err) => println(err)
      case Right(graph) => println (graph)
    }
  }
}
